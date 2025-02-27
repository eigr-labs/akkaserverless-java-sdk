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

package kalix.springsdk.impl.valueentity

import com.google.protobuf.any.{ Any => ScalaPbAny }
import kalix.javasdk.impl.valueentity.ValueEntityRouter
import kalix.javasdk.valueentity.CommandContext
import kalix.javasdk.valueentity.ValueEntity
import kalix.springsdk.impl.CommandHandler
import kalix.springsdk.impl.InvocationContext

class ReflectiveValueEntityRouter[S, E <: ValueEntity[S]](
    override protected val entity: E,
    commandHandlers: Map[String, CommandHandler])
    extends ValueEntityRouter[S, E](entity) {

  private def commandHandlerLookup(commandName: String) =
    commandHandlers.getOrElse(commandName, throw new RuntimeException(s"no matching method for '$commandName'"))

  override protected def handleCommand(
      commandName: String,
      state: S,
      command: Any,
      context: CommandContext): ValueEntity.Effect[_] = {

    // pass current state to entity
    entity._internalSetCurrentState(state)

    val commandHandler = commandHandlerLookup(commandName)
    val context =
      InvocationContext(command.asInstanceOf[ScalaPbAny], commandHandler.requestMessageDescriptor)

    val inputTypeUrl = command.asInstanceOf[ScalaPbAny].typeUrl

    commandHandler
      .getInvoker(inputTypeUrl)
      .invoke(entity, context)
      .asInstanceOf[ValueEntity.Effect[_]]
  }
}
