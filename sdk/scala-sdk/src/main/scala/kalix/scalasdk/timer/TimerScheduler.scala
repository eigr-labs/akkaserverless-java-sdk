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

package kalix.scalasdk.timer

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import akka.Done
import kalix.scalasdk.DeferredCall

trait TimerScheduler {

  /**
   * Schedule a single timer in Kalix. Timers allow for scheduling calls in the future. For example, to verify that some
   * process have been completed or not.
   *
   * Timers are persisted and are guaranteed to run at least once.
   *
   * When a timer is triggered, the scheduled call is executed. If successfully executed, the timer completes and is
   * automatically removed. In case of a failure, the timer is rescheduled with a delay of 3 seconds. This process
   * repeats until the call succeeds. Therefore, timers run at least once.
   *
   * Each timer has a `name` and if a new timer with same `name` is registered the previous is cancelled.
   *
   * @param name
   *   unique name for the timer
   * @param delay
   *   delay, starting from now, in which the timer should be triggered
   * @param deferredCall
   *   a call to component that will be executed when the timer is triggered
   */
  def startSingleTimer[I, O](name: String, delay: FiniteDuration, deferredCall: DeferredCall[I, O]): Future[Done]

  /**
   * Cancel an existing timer. This completes successfully if not timer is registered for the passed name.
   */
  def cancel(name: String): Future[Done]

}
