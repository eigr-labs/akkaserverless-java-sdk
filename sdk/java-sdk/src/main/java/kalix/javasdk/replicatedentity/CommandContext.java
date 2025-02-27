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

package kalix.javasdk.replicatedentity;

import kalix.javasdk.MetadataContext;

/** Command context for Replicated Entity. */
public interface CommandContext extends ReplicatedEntityContext, MetadataContext {
  /**
   * The id of the command. This is an internal ID generated by the proxy, and is unique to a given
   * entity stream. It may be used for debugging purposes.
   *
   * @return The ID of the command.
   */
  long commandId();

  /**
   * The name of the command.
   *
   * <p>Corresponds to the name of the rpc call in the protobuf definition.
   *
   * @return The name of the command.
   */
  String commandName();
}
