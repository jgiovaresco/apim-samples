package io.apim.samples.ports.grpc

import io.grpc.examples.helloworld.Greeter
import io.grpc.examples.helloworld.HelloRequest
import io.quarkus.grpc.GrpcClient
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@QuarkusTest
class GreeterGrpcServiceTest {

  @GrpcClient("greeter")
  lateinit var greeter: Greeter

  @Nested
  inner class SayHello {

    @Test
    fun `should greet when name provided`() {
      val message = HelloRequest.newBuilder().setName("John").build()

      val reply = greeter.sayHello(message)
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(reply) {
        get { reply.message }.isEqualTo("Hello John")
      }
    }

    @Test
    fun `should greet when no name provided`() {
      val message = HelloRequest.newBuilder().build()

      val reply = greeter.sayHello(message)
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(reply) {
        get { reply.message }.isEqualTo("Hello Stranger")
      }
    }
  }
}
