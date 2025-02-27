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

package com.example.wiring.eventsourcedentities.counter;

import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import kalix.javasdk.eventsourcedentity.EventSourcedEntityContext;
import kalix.springsdk.annotations.EntityKey;
import kalix.springsdk.annotations.EntityType;
import kalix.springsdk.annotations.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@EntityKey("id")
@EntityType("counter")
@RequestMapping("/counter/{id}")
public class CounterEntity extends EventSourcedEntity<Counter> {

  private Logger logger = LoggerFactory.getLogger(getClass());

  private final EventSourcedEntityContext context;

  public CounterEntity(EventSourcedEntityContext context) {
    this.context = context;
  }

  @Override
  public Counter emptyState() {
    return new Counter(0);
  }

  @PostMapping("/increase/{value}")
  public Effect<Integer> increase(@PathVariable Integer value) {
    return effects().emitEvent(new ValueIncreased(value)).thenReply(c -> c.value);
  }

  @GetMapping
  public Effect<String> get() {
    return effects().reply(currentState().value.toString());
  }

  @PostMapping("/multiply/{value}")
  public Effect<Integer> times(@PathVariable Integer value) {
    logger.info(
        "Increasing counter with commandId={} commandName={} seqNr={} current={} value={}",
        commandContext().commandId(),
        commandContext().commandName(),
        commandContext().sequenceNumber(),
        currentState(),
        value);

    return effects().emitEvent(new ValueMultiplied(value)).thenReply(c -> c.value);
  }

  @EventHandler
  public Counter handleIncrease(ValueIncreased increased) {
    return currentState().onValueIncreased(increased);
  }

  @EventHandler
  public Counter handleMultiply(ValueMultiplied multiplied) {
    return currentState().onValueMultiplied(multiplied);
  }
}
