package io.apim.samples.websocket

import io.apim.samples.webSocketPort
import io.reactivex.rxjava3.core.Completable
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.AbstractVerticle
import io.vertx.rxjava3.core.http.ServerWebSocket
import org.slf4j.LoggerFactory

class WebSocketServerVerticle(private val configRetriever: ConfigRetriever) :
  AbstractVerticle() {
  companion object {
    const val DEFAULT_PORT = 8890
  }

  private val logger = LoggerFactory.getLogger(javaClass)

  override fun rxStart(): Completable = configRetriever.config
    .map { it.getInteger(webSocketPort, DEFAULT_PORT) }
    .flatMap { port ->
      vertx
        .createHttpServer()
        .webSocketHandler(::routes)
        .listen(port)
    }
    .doOnSuccess {
      logger.info("WebSocket server started on port ${it.actualPort()}")
    }
    .ignoreElement()

  private fun routes(ws: ServerWebSocket) =
    when (ws.path()) {
      EchoHandler.ECHO_PATH -> EchoHandler(ws).handle()
      else -> ws.close(4404, "Not found")
    }.subscribe()
}
