package io.apim.samples.websocket

import io.reactivex.rxjava3.core.Completable
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.core.http.ServerWebSocket

class EchoHandler(private val ws: ServerWebSocket) : WebSockerHandler {
  companion object {
    const val ECHO_PATH = "/ws/echo"
  }

  override fun handle(): Completable {
    ws.binaryMessageHandler { buffer ->
      processMessage(ws, buffer.toString())
    }

    ws.textMessageHandler { msg ->
      processMessage(ws, msg)
    }

    return Completable.complete()
  }

  private fun processMessage(ws: ServerWebSocket, message: String) {
    ws.writeTextMessage(parseInput(message).toString())
      .subscribe()
  }

  private fun parseInput(input: String) = try {
    val json = JsonObject(input)
    json { obj("type" to "json", "request" to json) }
  } catch (e: Exception) {
    json { obj("type" to "unknown", "request" to input) }
  }
}
