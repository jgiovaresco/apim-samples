package io.apim.samples

import io.apim.samples.rest.RestVerticle
import io.reactivex.rxjava3.core.Completable
import io.vertx.core.Vertx
import io.vertx.ext.healthchecks.Status
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.AbstractVerticle
import io.vertx.rxjava3.ext.healthchecks.HealthChecks

class MainVerticle : AbstractVerticle() {
  override fun rxStart(): Completable {
    val configRetriever = ConfigRetriever.create(vertx)
    val healthChecks = HealthChecks.create(vertx)

    healthChecks.register("status") { promise -> promise.complete(Status.OK()) }

    return vertx.deployVerticle(RestVerticle(configRetriever, healthChecks)).ignoreElement()
  }
}

fun main() {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(MainVerticle())
}
