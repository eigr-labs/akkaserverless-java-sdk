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

package kalix.scalasdk.testkit.impl

import kalix.javasdk.impl.GrpcDeferredCall
import kalix.javasdk.impl.MetadataImpl
import kalix.javasdk.impl.effect.ForwardReplyImpl
import kalix.javasdk.impl.effect.MessageReplyImpl
import kalix.javasdk.impl.effect.SideEffectImpl
import kalix.scalasdk.impl.eventsourcedentity.EventSourcedEntityEffectImpl
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EventSourcedResultSpec extends AnyWordSpec with Matchers {

  "Event Sourced Entity Results" must {

    "extract side effects" in {
      val replyWithSideEffectResult = new EventSourcedResultImpl[String, String](
        EventSourcedEntityEffectImpl().reply("not actually used here"),
        "state",
        MessageReplyImpl(
          "reply", // pretend it was evaluated, in practice done by the generated testkit
          MetadataImpl.Empty,
          Vector(SideEffectImpl(
            GrpcDeferredCall[String, Any]("request", MetadataImpl.Empty, "full.service.Name", "MethodName", () => ???),
            synchronous = false))))

      replyWithSideEffectResult.isReply should ===(true)
      replyWithSideEffectResult.sideEffects should have size 1
    }

    "extract forward details" in {
      val forwardResult = new EventSourcedResultImpl[String, String](
        EventSourcedEntityEffectImpl().reply("not actually used here"),
        "state",
        ForwardReplyImpl(
          deferredCall =
            GrpcDeferredCall[String, Any]("request", MetadataImpl.Empty, "full.service.Name", "MethodName", () => ???),
          sideEffects = Vector.empty))

      forwardResult.isForward should ===(true)
      forwardResult.forwardedTo.message should ===("request")
    }

  }

}
