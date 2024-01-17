package io.apim.samples.grpc

import io.grpc.examples.helloworld.HelloReply
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.examples.helloworld.VertxGreeterGrpc
import io.grpc.examples.helloworld.helloReply
import io.grpc.examples.routeguide.*
import io.vertx.core.Future
import io.vertx.core.Promise
import io.vertx.core.json.JsonObject
import io.vertx.core.streams.ReadStream
import io.vertx.core.streams.WriteStream
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.TimeUnit.NANOSECONDS
import kotlin.math.*

class GreeterService() : VertxGreeterGrpc.GreeterVertxImplBase() {
  private val logger = LoggerFactory.getLogger(javaClass)
  private val routeNotes: ConcurrentMap<Point, MutableList<RouteNote>> = ConcurrentHashMap()


  override fun sayHello(request: HelloRequest): Future<HelloReply> {
    var name = request.name
    if(name.isBlank()) {
      name = "Stranger"
    }

    return Future.succeededFuture(helloReply { message = "Hello $name" })
  }
}
