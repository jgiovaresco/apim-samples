package io.apim.samples.ports.grpc

import io.grpc.examples.helloworld.Greeter
import io.grpc.examples.helloworld.HelloReply
import io.grpc.examples.helloworld.HelloRequest
import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Uni

@GrpcService
class GreeterGrpcService() : Greeter {
  override fun sayHello(request: HelloRequest): Uni<HelloReply> {
    var name = request.name
    if(name.isBlank()) {
      name = "Stranger"
    }

    return Uni.createFrom().item(HelloReply.newBuilder().setMessage("Hello $name").build())
  }
}
