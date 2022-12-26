package io.apim.samples.websocket

import io.reactivex.rxjava3.kotlin.subscribeBy
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.Vertx
import io.vertx.rxjava3.core.buffer.Buffer
import io.vertx.rxjava3.core.http.HttpClient
import io.vertx.rxjava3.ext.web.client.WebClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebSocketServerVerticleTest {
  private val vertx: Vertx = Vertx.vertx()
  private val configRetriever: ConfigRetriever = ConfigRetriever.create(vertx)

  lateinit var client: HttpClient

  @BeforeAll
  fun setUp(testContext: VertxTestContext) {
    vertx.deployVerticle(WebSocketServerVerticle(configRetriever))
      .subscribeBy { testContext.completeNow() }

    client = vertx.createHttpClient(
      HttpClientOptions()
        .setDefaultHost("localhost")
        .setDefaultPort(WebSocketServerVerticle.DEFAULT_PORT)
    )
  }

  @AfterAll
  fun tearDown(testContext: VertxTestContext) {
    vertx.close()
      .subscribeBy { testContext.completeNow() }
  }

  @Test
  fun `should reject connection on unexpected path`(context: VertxTestContext) {
    client.webSocket("/ws/unknown")
      .doOnSuccess { ws ->
        ws.endHandler {
          expectThat(ws) {
            get { isClosed }.isTrue()
            get { closeReason() }.isEqualTo("Not found")
            get { closeStatusCode() }.isEqualTo(4404)
          }
          context.completeNow()
        }
      }
      .test()
      .await()
      .assertComplete()
  }

  @Nested
  @ExtendWith(VertxExtension::class)
  inner class EchoHandlerTest {
    private val jsonRequest = json { obj("message" to "Hello") }
    private val unknownRequest = "unknown message"

    @Test
    fun `should reply to a json text message`(context: VertxTestContext) {

      client.webSocket(EchoHandler.ECHO_PATH)
        .flatMapCompletable {
          it.textMessageHandler { message ->
            checkJsonResponse(context, JsonObject(message), jsonRequest)
          }

          it.writeTextMessage(jsonRequest.toString())

        }
        .test()
        .await()
        .assertComplete()
    }

    @Test
    fun `should reply to a json binary message`(context: VertxTestContext) {
      client.webSocket(EchoHandler.ECHO_PATH)
        .flatMapCompletable {
          it.textMessageHandler { message ->
            checkJsonResponse(context, JsonObject(message), jsonRequest)
          }

          it.writeBinaryMessage(Buffer.buffer(jsonRequest.toString()))

        }
        .test()
        .await()
        .assertComplete()
    }

    @Test
    fun `should reply to an unknown text message`(context: VertxTestContext) {

      client.webSocket(EchoHandler.ECHO_PATH)
        .flatMapCompletable {
          it.textMessageHandler { message ->
            checkUnknownResponse(context, JsonObject(message))
          }

          it.writeTextMessage(unknownRequest)

        }
        .test()
        .await()
        .assertComplete()
    }

    @Test
    fun `should reply to an unknown binary message`(context: VertxTestContext) {

      client.webSocket(EchoHandler.ECHO_PATH)
        .flatMapCompletable {
          it.textMessageHandler { message ->
            checkUnknownResponse(context, JsonObject(message))
          }

          it.writeBinaryMessage(Buffer.buffer(unknownRequest))

        }
        .test()
        .await()
        .assertComplete()
    }

    private fun checkJsonResponse(context: VertxTestContext, actual: JsonObject, expected: JsonObject) {
      try {
        expectThat(actual) {
          get { getString("type") }.isEqualTo("json")
          get { getJsonObject("request") }.isEqualTo(expected)
        }
        context.completeNow()
      } catch (e: Throwable) {
        context.failNow(e)
      }
    }

    private fun checkUnknownResponse(context: VertxTestContext, actual: JsonObject) {
      try {
        expectThat(actual) {
          get { getString("type") }.isEqualTo("unknown")
          get { getString("request") }.isEqualTo(unknownRequest)
        }
        context.completeNow()
      } catch (e: Throwable) {
        context.failNow(e)
      }
    }
  }

  @Nested
  inner class HealthCheckHandler {
    @Test
    fun `should return the status healthcheck`() {
      WebClient.wrap(client)
        .get("/health").send()
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result) {
            get { statusCode() }.isEqualTo(200)
            get { bodyAsJsonObject() }.isEqualTo(json {
              obj(
                "checks" to array(
                  obj(
                    "id" to "status",
                    "status" to "UP"
                  )
                ),
                "status" to "UP",
                "outcome" to "UP"
              )
            })
          }
          true
        }
    }
  }
}
