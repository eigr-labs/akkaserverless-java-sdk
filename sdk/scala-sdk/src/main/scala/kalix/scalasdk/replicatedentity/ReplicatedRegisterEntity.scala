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

package kalix.scalasdk.replicatedentity

abstract class ReplicatedRegisterEntity[T] extends ReplicatedEntity[ReplicatedRegister[T]] {

  /**
   * Implement to set the default empty value for the register.
   *
   * @return
   *   the default empty value
   */
  def emptyValue: T

  /**
   * Implement by returning the initial empty replicated data object. This object will be passed into the command
   * handlers.
   *
   * Also known as the "zero" or "neutral" state.
   *
   * The initial data cannot be `null`.
   *
   * @param factory
   *   the factory to create the initial empty replicated data object
   */
  override final def emptyData(factory: ReplicatedDataFactory): ReplicatedRegister[T] =
    factory.newRegister(emptyValue)
}
