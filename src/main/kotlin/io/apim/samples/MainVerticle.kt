package io.apim.samples

import io.apim.samples.rest.RestVerticle
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.ext.healthchecks.HealthChecks
import io.vertx.ext.healthchecks.Status
import io.vertx.kotlin.coroutines.CoroutineVerticle

class MainVerticle : CoroutineVerticle() {
  override suspend fun start() {
    val configRetriever = ConfigRetriever.create(vertx)
    val healthChecks = HealthChecks.create(vertx)
    healthChecks.register("status") { promise -> promise.complete(Status.OK()) }

    vertx.deployVerticle(RestVerticle(configRetriever, healthChecks))
  }
}

fun main() {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(MainVerticle())
}
