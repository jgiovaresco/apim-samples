package io.apim.samples.ports.grpc

import io.grpc.examples.helloworld.HelloReply
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.examples.stream.StreamReply
import io.grpc.examples.stream.StreamRequest
import io.grpc.examples.stream.StreamService
import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime

@GrpcService
class StreamGrpcService() : StreamService {
  override fun stream(request: StreamRequest): Multi<StreamReply> {
    val delay = if (request.delayInSeconds > 0) request.delayInSeconds else 1
    val number = if (request.numberOfReplies > 0) request.numberOfReplies else 5

    val ticks = Multi.createFrom().ticks().every(Duration.ofSeconds(delay.toLong()))
    val messages = Multi.createFrom().range(0, number)
    3
    return Multi.createBy().combining().streams(ticks, messages).using { _, message ->
      StreamReply.newBuilder()
        .setTimestamp(Instant.now().toEpochMilli())
        .setMessage("${ZonedDateTime.now()} - message $message").build()
    }
  }

  fun sayHello(request: HelloRequest): Uni<HelloReply> {
    var name = request.name
    if (name.isBlank()) {
      name = "Stranger"
    }

    return Uni.createFrom().item(HelloReply.newBuilder().setMessage("Hello $name").build())
  }
}
