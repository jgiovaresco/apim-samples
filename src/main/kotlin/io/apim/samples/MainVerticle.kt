package io.apim.samples

import io.apim.samples.rest.RestVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.CoroutineVerticle

class MainVerticle : CoroutineVerticle() {
  override suspend fun start() {
    vertx.deployVerticle(RestVerticle())
  }
}

fun main() {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(MainVerticle())
}
