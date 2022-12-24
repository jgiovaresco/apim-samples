package io.apim.samples.rest

import io.apim.samples.httpPort
import io.vertx.config.ConfigRetriever
import io.vertx.ext.healthchecks.HealthCheckHandler
import io.vertx.ext.healthchecks.HealthChecks
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch

class RestVerticle(private val configRetriever: ConfigRetriever, private val healthChecks: HealthChecks) : CoroutineVerticle() {

  override suspend fun start() {
    val router = router()
    val config = configRetriever.config.await()
    val port = config.getInteger(httpPort, 8888)

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(port).await()

    println("HTTP server started on port $port")
  }

  private fun router(): Router {
    val router = Router.router(vertx)
    val healthCheckHandler = HealthCheckHandler.createWithHealthChecks(healthChecks)
    router.route().handler(BodyHandler.create())

    router.route("/echo").coroutineHandler(::echoHandler)
    router.route("/health*").handler(healthCheckHandler)

    return router
  }

  /** see issue https://github.com/vert-x3/vertx-lang-kotlin/issues/194 */
  private fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) = handler {
    launch(it.vertx().dispatcher()) {
      try {
        fn(it)
      } catch (e: Exception) {
        it.fail(e)
      }
    }
  }
}
