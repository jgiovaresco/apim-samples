package io.apim.samples.rest

import io.apim.samples.httpPort
import io.reactivex.rxjava3.core.Completable
import io.vertx.ext.healthchecks.Status
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.AbstractVerticle
import io.vertx.rxjava3.ext.healthchecks.HealthCheckHandler
import io.vertx.rxjava3.ext.healthchecks.HealthChecks
import io.vertx.rxjava3.ext.web.Router
import io.vertx.rxjava3.ext.web.handler.BodyHandler
import org.slf4j.LoggerFactory

class RestServerVerticle(private val configRetriever: ConfigRetriever) :
  AbstractVerticle() {
  companion object {
    const val DEFAULT_PORT = 8888
  }

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun rxStart(): Completable = configRetriever.config
    .map { it.getInteger(httpPort, DEFAULT_PORT) }
    .flatMap { port ->
      vertx
        .createHttpServer()
        .requestHandler(router())
        .listen(port)
    }
    .doOnSuccess {
      logger.info("HTTP server started on port ${it.actualPort()}")
    }
    .ignoreElement()

  private fun router(): Router = Router.router(vertx).let { router ->
    router.route().handler(BodyHandler.create())
    router.route("/echo").handler(::echoHandler)
    router.route("/grpc*").handler(::protobufFileHandler)
    router.route("/health*").handler(HealthCheckHandler.createWithHealthChecks(healthChecks()))
    router
  }

  private fun healthChecks(): HealthChecks =
    HealthChecks.create(vertx).register("status") { promise -> promise.complete(Status.OK()) }
}
