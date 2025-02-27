package org.example;

import kalix.javasdk.Context;
import kalix.javasdk.DeferredCall;
import kalix.javasdk.impl.GrpcDeferredCall;
import kalix.javasdk.impl.InternalContext;
import kalix.javasdk.impl.MetadataImpl;

// This code is managed by Kalix tooling.
// It will be re-generated to reflect any changes to your protobuf definitions.
// DO NOT EDIT

/**
 * Not intended for direct instantiation, called by generated code, use Action.components() to access
 */
public final class ComponentsImpl implements Components {

  private final InternalContext context;

  public ComponentsImpl(Context context) {
    this.context = (InternalContext) context;
  }

  private <T> T getGrpcClient(Class<T> serviceClass) {
    return context.getComponentGrpcClient(serviceClass);
  }

  @Override
  public Components.CounterCalls counter() {
    return new CounterCallsImpl();
  }

  private final class CounterCallsImpl implements Components.CounterCalls {
     @Override
    public DeferredCall<org.example.eventsourcedentity.CounterApi.IncreaseValue, com.google.protobuf.Empty> increase(org.example.eventsourcedentity.CounterApi.IncreaseValue increaseValue) {
      return new GrpcDeferredCall<>(
        increaseValue,
        MetadataImpl.Empty(),
        "org.example.eventsourcedentity.CounterService",
        "Increase",
        () -> getGrpcClient(org.example.eventsourcedentity.CounterService.class).increase(increaseValue)
      );
    }
    @Override
    public DeferredCall<org.example.eventsourcedentity.CounterApi.DecreaseValue, com.google.protobuf.Empty> decrease(org.example.eventsourcedentity.CounterApi.DecreaseValue decreaseValue) {
      return new GrpcDeferredCall<>(
        decreaseValue,
        MetadataImpl.Empty(),
        "org.example.eventsourcedentity.CounterService",
        "Decrease",
        () -> getGrpcClient(org.example.eventsourcedentity.CounterService.class).decrease(decreaseValue)
      );
    }
  }
}
