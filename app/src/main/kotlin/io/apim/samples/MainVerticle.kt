package io.apim.samples

import io.apim.samples.grpc.GrpcServerVerticle
import io.apim.samples.rest.RestServerVerticle
import io.apim.samples.websocket.WebSocketServerVerticle
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.vertx.core.Vertx
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.AbstractVerticle

class MainVerticle : AbstractVerticle() {
  override fun rxStart(): Completable {
    val configRetriever = ConfigRetriever.create(vertx)

    return Single.merge(
      vertx.deployVerticle(WebSocketServerVerticle(configRetriever)),
      vertx.deployVerticle(RestServerVerticle(configRetriever)),
      vertx.deployVerticle(GrpcServerVerticle(configRetriever)),
    ).ignoreElements()
  }
}

fun main() {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(MainVerticle())
}
