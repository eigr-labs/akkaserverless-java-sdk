/* This code was initialised by Kalix tooling.
 * As long as this file exists it will not be re-generated.
 * You are free to make changes to this file.
 */

package com.example;

import com.example.CounterApi;
import com.example.CounterService;
import com.example.Main;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import kalix.javasdk.testkit.junit.KalixTestKitResource;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

// Run all test classes ending with "IntegrationTest" using `mvn verify -Pit`
// tag::sample-it-test[]
public class CounterIntegrationTest {

  /**
   * The test kit starts both the service container and the Kalix proxy.
   */
  @ClassRule
  public static final KalixTestKitResource testKit =
          new KalixTestKitResource(Main.createKalix());

  /**
   * Use the generated gRPC client to call the service through the Kalix proxy.
   */
  private final CounterService client;

  public CounterIntegrationTest() {
      client = testKit.getGrpcClient(CounterService.class);
  }

  @Test
  public void increaseOnNonExistingEntity() throws Exception {
      String entityId = "new-id";
      client.increase(CounterApi.IncreaseValue.newBuilder().setCounterId(entityId).setValue(42).build())
               .toCompletableFuture().get(5, SECONDS);
      CounterApi.CurrentCounter reply = client.getCurrentCounter(CounterApi.GetCounter.newBuilder().setCounterId(entityId).build())
              .toCompletableFuture().get(5, SECONDS);
      assertThat(reply.getValue(), is(42));
  }
  // end::sample-it-test[]

  @Test
  public void increase() throws Exception {
      String entityId = "another-id";
      client.increase(CounterApi.IncreaseValue.newBuilder().setCounterId(entityId).setValue(42).build())
               .toCompletableFuture().get(5, SECONDS);
      client.increase(CounterApi.IncreaseValue.newBuilder().setCounterId(entityId).setValue(27).build())
               .toCompletableFuture().get(5, SECONDS);
      CounterApi.CurrentCounter reply = client.getCurrentCounter(CounterApi.GetCounter.newBuilder().setCounterId(entityId).build())
              .toCompletableFuture().get(5, SECONDS);
      assertThat(reply.getValue(), is(69));
  }

    @Test
    public void validateGrpcStatusCode() throws Exception {
        try {
            client.increaseWithConditional(CounterApi.IncreaseValue.newBuilder().setCounterId("new-id").setValue(-10).build())
                    .toCompletableFuture().get(5, SECONDS);
            Assert.fail("Previous method should be invalid and throw exception");
        } catch (ExecutionException e) {
            var suppressed = (StatusRuntimeException) e.getCause();
            assertThat(suppressed.getStatus().getCode(), is(Status.Code.INVALID_ARGUMENT));
        }
    }

}
