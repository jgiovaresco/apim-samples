package io.apim.samples

import io.apim.samples.rest.RestVerticle
import io.reactivex.rxjava3.core.Completable
import io.vertx.core.Vertx
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.AbstractVerticle

class MainVerticle : AbstractVerticle() {
  override fun rxStart(): Completable {
    val configRetriever = ConfigRetriever.create(vertx)

    return vertx.deployVerticle(RestVerticle(configRetriever)).ignoreElement()
  }
}

fun main() {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(MainVerticle())
}
