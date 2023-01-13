package io.apim.samples

import io.apim.samples.grpc.GrpcServerVerticle
import io.apim.samples.rest.RestServerVerticle
import io.apim.samples.websocket.WebSocketServerVerticle
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.vertx.core.Vertx
import io.vertx.ext.healthchecks.Status
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.AbstractVerticle
import io.vertx.rxjava3.ext.healthchecks.HealthChecks

class MainVerticle : AbstractVerticle() {
  override fun rxStart(): Completable {
    val configRetriever = ConfigRetriever.create(vertx)

    return buildHealthChecks(configRetriever)
      .flatMapCompletable { healthChecks ->
        Single.merge(
          vertx.deployVerticle(WebSocketServerVerticle(configRetriever)),
          vertx.deployVerticle(GrpcServerVerticle(configRetriever)),
          vertx.deployVerticle(RestServerVerticle(configRetriever, healthChecks)),
        ).ignoreElements()
      }
  }

  private fun buildHealthChecks(configRetriever: ConfigRetriever): Single<HealthChecks> {
    val client = vertx.createNetClient()

    return configRetriever.config
      .map {
        HealthChecks.create(vertx)
          .register("websocket") { promise ->
            client.connect(it.getInteger(webSocketPort, WebSocketServerVerticle.DEFAULT_PORT), "0.0.0.0")
              .doOnSuccess { promise.complete(Status.OK()) }
              .doOnError { promise.complete(handleConnectionError(it)) }
              .flatMapCompletable { it.close() }
              .subscribe()
          }
          .register("grpc") { promise ->
            client.connect(it.getInteger(grpcPort, GrpcServerVerticle.DEFAULT_PORT), "0.0.0.0")
              .doOnSuccess { promise.complete(Status.OK()) }
              .doOnError { promise.complete(handleConnectionError(it)) }
              .flatMapCompletable { it.close() }
              .subscribe()
          }
      }
  }

  private fun handleConnectionError(throwable: Throwable): Status {
    val error = json { obj("message" to throwable.message) }
    return Status.KO(error)
  }
}

fun main() {
  val vertx = Vertx.vertx()
  vertx.deployVerticle(MainVerticle())
}
