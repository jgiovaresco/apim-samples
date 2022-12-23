package io.apim.samples

import io.apim.samples.rest.RestVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.CoroutineVerticle

class MainVerticle : CoroutineVerticle() {
  override suspend fun start() {
    val configRetriever = ConfigRetriever.create(vertx)

    vertx.deployVerticle(RestVerticle(configRetriever))
  }
}

fun main() {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(MainVerticle())
}
