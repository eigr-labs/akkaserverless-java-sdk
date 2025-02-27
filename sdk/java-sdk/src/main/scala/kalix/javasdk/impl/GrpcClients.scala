/*
 * Copyright 2021 Lightbend Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kalix.javasdk.impl

import akka.Done
import akka.actor.ClassicActorSystemProvider
import akka.actor.CoordinatedShutdown
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.grpc.GrpcClientSettings
import akka.grpc.javadsl.{ AkkaGrpcClient => AkkaGrpcJavaClient }
import akka.grpc.scaladsl.{ AkkaGrpcClient => AkkaGrpcScalaClient }
import org.slf4j.LoggerFactory
import java.util.concurrent.{ ConcurrentHashMap, Executor }

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.jdk.FutureConverters._
import akka.actor.ActorSystem
import io.grpc.{ CallCredentials, Metadata }
import kalix.javasdk.Principal
import kalix.protocol.discovery.IdentificationInfo

/**
 * INTERNAL API
 */
object GrpcClients extends ExtensionId[GrpcClients] with ExtensionIdProvider {
  override def get(system: ActorSystem): GrpcClients = super.get(system)

  override def get(system: ClassicActorSystemProvider): GrpcClients = super.get(system)

  override def createExtension(system: ExtendedActorSystem): GrpcClients =
    new GrpcClients(system)
  override def lookup: ExtensionId[_ <: Extension] = this

  final private case class Key(serviceClass: Class[_], service: String, port: Int, addHeader: Option[(String, String)])
}

/**
 * INTERNAL API
 */
final class GrpcClients(system: ExtendedActorSystem) extends Extension {
  import GrpcClients._
  private val log = LoggerFactory.getLogger(classOf[GrpcClients])

  @volatile private var proxyHostname: Option[String] = None
  @volatile private var proxyPort: Option[Int] = None
  @volatile private var identificationInfo: Option[IdentificationInfo] = None
  private implicit val ec: ExecutionContext = system.dispatcher
  private val clients = new ConcurrentHashMap[Key, AnyRef]()

  CoordinatedShutdown(system).addTask(CoordinatedShutdown.PhaseServiceStop, "stop-grpc-clients")(() =>
    Future
      .traverse(clients.values().asScala) {
        case javaClient: AkkaGrpcJavaClient   => javaClient.close().asScala
        case scalaClient: AkkaGrpcScalaClient => scalaClient.close()
      }
      .map(_ => Done))

  def setProxyHostname(hostname: String): Unit = {
    log.debug("Setting proxy hostname to: [{}]", hostname)
    proxyHostname = Some(hostname)
  }

  def setIdentificationInfo(info: Option[IdentificationInfo]): Unit = {
    log.debug("Setting identification info to name to: [{}]", info)
    identificationInfo = info
  }

  def setProxyPort(port: Int): Unit = {
    log.debug("Setting port to: [{}]", port)
    proxyPort = Some(port)
  }

  def getComponentGrpcClient[T](serviceClass: Class[T]): T = {
    getProxyGrpcClient(serviceClass)
  }
  def getProxyGrpcClient[T](serviceClass: Class[T]): T = {
    getLocalGrpcClient(serviceClass)
  }

  // FIXME we might be able to revert this once we implement transcoding of Rest calls to Grpc calls so this is not needed outside
  def getProxyHostname: Option[String] = proxyHostname
  def getProxyPort: Option[Int] = proxyPort
  def getIdentificationInfo: Option[IdentificationInfo] = identificationInfo

  /**
   * This gets called from the action context to get a client to another service, and hence needs to add a service
   * identification header (in dev/test mode) to ensure calls get associated with this service.
   */
  def getGrpcClient[T](serviceClass: Class[T], service: String): T =
    getGrpcClient(serviceClass, service, port = 80, remoteAddHeader)

  /** gRPC clients point to services (user components or Kalix services) in the same deployable */
  private def getLocalGrpcClient[T](serviceClass: Class[T]): T = {
    (proxyHostname, proxyPort) match {
      case (Some(internalProxyHostname), Some(port)) =>
        getGrpcClient(serviceClass, internalProxyHostname, port, localAddHeader)
      // for backward compatibiliy with proxy 1.0.14 or older.
      case (Some("localhost"), None) =>
        log.warn("you are using an old version of the Kalix proxy")
        getGrpcClient(serviceClass, "localhost", proxyPort.getOrElse(9000), localAddHeader)
      // for backward compatibiliy with proxy 1.0.14 or older
      case (Some(proxyHostname), None) =>
        log.warn("you are using an old version of the Kalix proxy")
        getGrpcClient(serviceClass, proxyHostname, 80, localAddHeader)
      case _ =>
        throw new IllegalStateException(
          "Service proxy hostname and port are not set by proxy at discovery, too old proxy version?")
    }
  }

  private def localAddHeader: Option[(String, String)] = identificationInfo match {
    case Some(IdentificationInfo(header, token, _, _, _)) if header.nonEmpty && token.nonEmpty =>
      Some((header, token))
    case _ => None
  }

  private def remoteAddHeader: Option[(String, String)] = identificationInfo match {
    case Some(IdentificationInfo(_, _, header, name, _)) if header.nonEmpty && name.nonEmpty =>
      Some((header, name))
    case _ => None
  }

  /** This gets called by the testkit, so shouldn't add any headers. */
  def getGrpcClient[T](serviceClass: Class[T], service: String, port: Int): T =
    getGrpcClient(serviceClass, service, port, None)

  /** This gets called by the testkit, and should impersonate the given principal. */
  def getGrpcClient[T](serviceClass: Class[T], service: String, port: Int, impersonate: String): T =
    getGrpcClient(serviceClass, service, port, Some("impersonate-kalix-service", impersonate))

  private def getGrpcClient[T](
      serviceClass: Class[T],
      service: String,
      port: Int,
      addHeader: Option[(String, String)]) = {
    clients.computeIfAbsent(Key(serviceClass, service, port, addHeader), createClient(_)).asInstanceOf[T]
  }

  private def createClient(key: Key): AnyRef = {
    val settings = if (!system.settings.config.hasPath(s"""akka.grpc.client."${key.service}"""")) {
      // "service" is not present in the config, treat it as an Akka gRPC inter-service call
      log.debug("Creating gRPC client for Kalix service [{}:{}]", key.service, key.port)
      GrpcClientSettings
        .connectToServiceAt(key.service, key.port)(system)
        // (TLS is handled for us by Kalix infra)
        .withTls(false)
    } else {
      log.debug("Creating gRPC client for external service [{}]", key.service)
      // external service, defined in config
      GrpcClientSettings.fromConfig(key.service)(system)
    }

    val settingsWithCallCredentials = key.addHeader match {
      case Some((key, value)) =>
        val headers = new Metadata()
        headers.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value)
        settings.withCallCredentials(new CallCredentials {
          override def applyRequestMetadata(
              requestInfo: CallCredentials.RequestInfo,
              appExecutor: Executor,
              applier: CallCredentials.MetadataApplier): Unit = {
            applier.apply(headers)
          }
          override def thisUsesUnstableApi(): Unit = ()
        })
      case None => settings
    }

    // expected to have a ServiceNameClient generated in the same package, so look that up through reflection
    val clientClass = system.dynamicAccess.getClassFor[AnyRef](key.serviceClass.getName + "Client").get
    val client =
      if (classOf[AkkaGrpcJavaClient].isAssignableFrom(clientClass)) {
        // Java API - static create
        val create = clientClass.getMethod("create", classOf[GrpcClientSettings], classOf[ClassicActorSystemProvider])
        create.invoke(null, settingsWithCallCredentials, system)
      } else if (classOf[AkkaGrpcScalaClient].isAssignableFrom(clientClass)) {
        // Scala API - companion object apply
        val companion = system.dynamicAccess.getObjectFor[AnyRef](key.serviceClass.getName + "Client").get
        val create =
          companion.getClass.getMethod("apply", classOf[GrpcClientSettings], classOf[ClassicActorSystemProvider])
        create.invoke(companion, settingsWithCallCredentials, system)
      } else {
        throw new IllegalArgumentException(s"Expected an AkkaGrpcClient but was [${clientClass.getName}]")
      }

    val closeDone = client match {
      case javaClient: AkkaGrpcJavaClient =>
        javaClient.closed().asScala
      case scalaClient: AkkaGrpcScalaClient =>
        scalaClient.closed
    }
    closeDone.foreach { _ =>
      // if the client is closed, remove it from the pool
      log.debug("gRPC client for service [{}] was closed", key.service)
      clients.remove(key)
    }

    client
  }

}
