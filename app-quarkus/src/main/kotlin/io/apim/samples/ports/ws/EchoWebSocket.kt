package io.apim.samples.ports.ws

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import jakarta.websocket.*
import jakarta.websocket.server.ServerEndpoint

@ServerEndpoint("/ws/echo")
class EchoWebSocket {
  @OnMessage
  fun processTextMessage(message: String, session: Session) {
    processMessage(message, session)
  }

  @OnMessage
  fun processBinaryMessage(message: ByteArray, session: Session) {
    processMessage(message.decodeToString(), session)
  }

  private fun processMessage(message: String, session: Session) {
    session.asyncRemote.sendText(parseInput(message).toString()) { result ->
      if (result.exception != null) {
        System.err.println("Unable to send message: " + result.exception)
      }
    }
  }

  private fun parseInput(input: String) = try {
    val json = JsonObject(input)
    json { obj("type" to "json", "request" to json) }
  } catch (e: Exception) {
    json { obj("type" to "unknown", "request" to input) }
  }
}
