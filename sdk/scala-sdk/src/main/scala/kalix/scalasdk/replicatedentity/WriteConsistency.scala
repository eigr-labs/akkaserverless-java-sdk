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

/** Write consistency setting for replication of state updates for Replicated Entities. */
sealed trait WriteConsistency

object WriteConsistency {

  /**
   * Updates will only be written to the local replica immediately, and then asynchronously distributed to other
   * replicas in the background.
   */
  case object Local extends WriteConsistency

  /**
   * Updates will be written immediately to a majority of replicas, and then asynchronously distributed to remaining
   * replicas in the background.
   */
  case object Majority extends WriteConsistency

  /** Updates will be written immediately to all replicas. */
  case object All extends WriteConsistency
}
