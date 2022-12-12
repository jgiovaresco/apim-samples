package io.apim.samples.rest

import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.launch

class RestVerticle : CoroutineVerticle() {

  override suspend fun start() {
    val router = router()

    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8888).await()
    println("HTTP server started on port 8888")
  }

  private fun router(): Router {
    val router = Router.router(vertx)
    router.route().handler(BodyHandler.create())

    router.route("/echo").coroutineHandler(::echoHandler)

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
