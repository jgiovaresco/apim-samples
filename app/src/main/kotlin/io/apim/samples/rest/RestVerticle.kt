package io.apim.samples.rest

import io.apim.samples.httpPort
import io.reactivex.rxjava3.core.Completable
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.AbstractVerticle
import io.vertx.rxjava3.ext.healthchecks.HealthCheckHandler
import io.vertx.rxjava3.ext.healthchecks.HealthChecks
import io.vertx.rxjava3.ext.web.Router
import io.vertx.rxjava3.ext.web.handler.BodyHandler
import org.slf4j.LoggerFactory

class RestVerticle(private val configRetriever: ConfigRetriever, private val healthChecks: HealthChecks) :
  AbstractVerticle() {
  private val logger = LoggerFactory.getLogger(javaClass)

  override fun rxStart(): Completable = configRetriever.config
    .map { it.getInteger(httpPort, 8888) }
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

  private fun router(): Router {
    val router = Router.router(vertx)
    val healthCheckHandler = HealthCheckHandler.createWithHealthChecks(healthChecks)
    router.route().handler(BodyHandler.create())

    router.route("/echo").handler(::echoHandler)
    router.route("/health*").handler(healthCheckHandler)

    return router
  }
}
