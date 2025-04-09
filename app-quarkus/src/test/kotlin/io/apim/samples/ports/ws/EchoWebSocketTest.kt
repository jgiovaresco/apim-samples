package io.apim.samples.ports.ws

import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Uni
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import io.vertx.core.http.UpgradeRejectedException
import io.vertx.core.http.WebSocketClientOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.core.http.WebSocketClient
import jakarta.inject.Inject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.net.URI


@QuarkusTest
class EchoWebSocketTest {

  @Inject
  lateinit var vertx: Vertx

  @TestHTTPResource("/ws/echo")
  lateinit var uri: URI

  lateinit var client: WebSocketClient

  private val jsonRequest = json { obj("message" to "Hello") }
  private val unknownRequest = "unknown message"

  @BeforeEach
  fun setUp() {
    client = vertx.createWebSocketClient(WebSocketClientOptions()
      .setDefaultHost(uri.host)
      .setDefaultPort(uri.port)
    )
  }

  @Test
  fun `should reply to a json text message`() {
    val response = client.connect(uri.path)
      .onItem().transformToUni { session ->
        Uni.createFrom().emitter { e ->
          session.textMessageHandler { message -> e.complete(message) }

          session.writeTextMessage(jsonRequest.encode())
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
        }
      }
      .subscribe()
      .withSubscriber(UniAssertSubscriber.create())
      .awaitItem()
      .item

    checkJsonResponse(JsonObject(response), jsonRequest)
  }

  @Test
  fun `should reply to a json binary message`() {
    val response = client.connect(uri.path)
      .onItem().transformToUni { session ->
        Uni.createFrom().emitter { e ->
          session.textMessageHandler { message -> e.complete(message) }

          session.writeBinaryMessage(Buffer.buffer(jsonRequest.encode()))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
        }
      }
      .subscribe()
      .withSubscriber(UniAssertSubscriber.create())
      .awaitItem()
      .item

    checkJsonResponse(JsonObject(response), jsonRequest)
  }

  @Test
  fun `should reply to an unknown text message`() {
    val response = client.connect(uri.path)
      .onItem().transformToUni { session ->
        Uni.createFrom().emitter { e ->
          session.textMessageHandler { message -> e.complete(message) }

          session.writeTextMessage(unknownRequest)
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
        }
      }
      .subscribe()
      .withSubscriber(UniAssertSubscriber.create())
      .awaitItem()
      .item

    checkUnknownResponse(JsonObject(response))
  }

  @Test
  fun `should reply to an unknown binary message`() {
    val response = client.connect(uri.path)
      .onItem().transformToUni { session ->
        Uni.createFrom().emitter { e ->
          session.textMessageHandler { message -> e.complete(message) }

          session.writeBinaryMessage(Buffer.buffer(unknownRequest))
            .subscribe()
            .withSubscriber(UniAssertSubscriber.create())
            .awaitItem()
        }
      }
      .subscribe()
      .withSubscriber(UniAssertSubscriber.create())
      .awaitItem()
      .item

    checkUnknownResponse(JsonObject(response))
  }

  @Test
  fun `should reject connection on unexpected path`() {
    val socket = client.connect("/ws/unknown")
      .subscribe()
      .withSubscriber(UniAssertSubscriber.create())
      .awaitFailure()
      .failure

    expectThat(socket).isA<UpgradeRejectedException>().and {
      get { status }.isEqualTo(404)
    }
  }


  private fun checkJsonResponse(actual: JsonObject, expected: JsonObject) {
    expectThat(actual) {
      get { getString("type") }.isEqualTo("json")
      get { getJsonObject("request") }.isEqualTo(expected)
    }
  }

  private fun checkUnknownResponse(actual: JsonObject) {
    expectThat(actual) {
      get { getString("type") }.isEqualTo("unknown")
      get { getString("request") }.isEqualTo(unknownRequest)
    }
  }
}
