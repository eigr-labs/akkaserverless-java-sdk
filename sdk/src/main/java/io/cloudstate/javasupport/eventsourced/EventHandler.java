package io.cloudstate.javasupport.eventsourced;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event handler.
 *
 * Methods annotated with this may take an {{@link EventBehaviorContext}},
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {
    /**
     * The event class. Generally, this will be determined by looking at the parameter of the event handler method,
     * however if the event doesn't need to be passed to the method (for example, perhaps it contains no data), then
     * this can be used to indicate which event this handler handles.
     */
    Class<?> eventClass() default Object.class;
}
