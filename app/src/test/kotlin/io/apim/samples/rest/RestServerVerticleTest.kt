package io.apim.samples.rest

import io.reactivex.rxjava3.kotlin.subscribeBy
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.Vertx
import io.vertx.rxjava3.ext.web.client.WebClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

@ExtendWith(VertxExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestServerVerticleTest {
  private val vertx: Vertx = Vertx.vertx()
  private val configRetriever: ConfigRetriever = ConfigRetriever.create(vertx)

  lateinit var client: WebClient

  @BeforeAll
  fun setUp(testContext: VertxTestContext) {
    vertx.deployVerticle(RestServerVerticle(configRetriever))
      .subscribeBy { testContext.completeNow() }

    client = WebClient.create(
      vertx,
      WebClientOptions()
        .setDefaultHost("localhost")
        .setDefaultPort(RestServerVerticle.DEFAULT_PORT)
    )
  }

  @AfterAll
  fun tearDown(testContext: VertxTestContext) {
    vertx.close()
      .subscribeBy { testContext.completeNow() }
  }

  @Nested
  @ExtendWith(VertxExtension::class)
  inner class EchoHandler {
    @Test
    fun `should return GET request in response body`() {
      client.get("/echo").send()
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result.bodyAsJsonObject()) {
            get { getString("method") }.isEqualTo("GET")

            and {
              get { getJsonObject("headers").getString("user-agent") }.contains("Vert.x-WebClient")
              get { getJsonObject("headers").getString("host") }.isEqualTo("localhost:8888")
            }
          }
          true
        }
    }

    @Test
    fun `should return GET request with query string in response body`() {
      client.get("/echo")
        .addQueryParam("param1", "value1")
        .addQueryParam("param2", "value2")
        .send()
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result.bodyAsJsonObject()) {
            get { getJsonObject("query_params").getString("param1") }.isEqualTo("value1")
            get { getJsonObject("query_params").getString("param2") }.isEqualTo("value2")
          }
          true
        }
    }

    @Test
    fun `should return POST request in response body`() {
      val body = json {
        obj(
          "message" to "hello!",
          "attribute" to "value"
        )
      }

      client.post("/echo")
        .sendJsonObject(body)
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result.bodyAsJsonObject()) {
            get { getString("method") }.isEqualTo("POST")

            and {
              get { getJsonObject("headers").getString("user-agent") }.contains("Vert.x-WebClient")
              get { getJsonObject("headers").getString("host") }.isEqualTo("localhost:8888")
              get { getJsonObject("headers").getString("content-type") }.isEqualTo("application/json")
              get { getJsonObject("headers").getString("content-length") }.isEqualTo(body.toString().length.toString())
            }

            and {
              get { getJsonObject("body") }.isEqualTo(body)
            }
          }
          true
        }
    }
  }

  @Nested
  inner class HealthCheckHandler {
    @Test
    fun `should return the status healthcheck`() {
      client.get("/health")
        .send()
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
