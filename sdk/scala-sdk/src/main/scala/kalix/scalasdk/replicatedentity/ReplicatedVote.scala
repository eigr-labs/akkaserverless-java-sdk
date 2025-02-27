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

import kalix.javasdk.impl.replicatedentity.ReplicatedVoteImpl
import kalix.protocol.replicated_entity.ReplicatedEntityDelta

/**
 * A Vote replicated data type.
 *
 * This replicated data type is used to allow all the nodes in a cluster to vote on a condition.
 */
class ReplicatedVote private[scalasdk] (override val delegate: ReplicatedVoteImpl) extends InternalReplicatedData {

  /**
   * Get the current value for this node's vote.
   *
   * @return
   *   this node's vote
   */
  def selfVote: Boolean = delegate.getSelfVote

  /**
   * Get the number of voters participating in the vote (ie, the number of nodes in the cluster).
   *
   * @return
   *   the number of voters
   */
  def voters: Int = delegate.getVoters

  /**
   * Get the number of votes for.
   *
   * @return
   *   the number of votes for
   */
  def votesFor: Int = delegate.getVotesFor

  /**
   * Update this node's vote to the given value.
   *
   * @param vote
   *   the vote this node is contributing
   * @return
   *   a new vote, or this unchanged vote
   */
  def vote(vote: Boolean): ReplicatedVote =
    new ReplicatedVote(delegate.vote(vote))

  /**
   * Has at least one node voted true?
   *
   * @return
   *   `true` if at least one node has voted true
   */
  def isAtLeastOne: Boolean = delegate.isAtLeastOne

  /**
   * Have a majority of nodes voted true?
   *
   * @return
   *   `true` if more than half of the nodes have voted true
   */
  def isMajority: Boolean = delegate.isMajority

  /**
   * Is the vote unanimous?
   *
   * @return
   *   `true` if all nodes have voted true
   */
  def isUnanimous: Boolean = delegate.isUnanimous

  final override type Self = ReplicatedVote

  final override def resetDelta(): ReplicatedVote =
    new ReplicatedVote(delegate.resetDelta())

  final override def applyDelta: PartialFunction[ReplicatedEntityDelta.Delta, ReplicatedVote] =
    delegate.applyDelta.andThen(new ReplicatedVote(_))
}
