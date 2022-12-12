package io.apim.samples.rest

import io.vertx.core.Vertx
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RestTest {
  private val vertx: Vertx = Vertx.vertx()
  lateinit var client: WebClient

  @BeforeAll
  fun setUp() {
    runBlocking {
      vertx.deployVerticle(RestVerticle()).await()
      client = WebClient.create(
        vertx,
        WebClientOptions()
          .setDefaultHost("localhost")
          .setDefaultPort(8888)
      )
    }
  }

  @AfterAll
  fun tearDown() {
    vertx.close()
  }

  @Nested
  inner class EchoHandler {
    @Test
    fun `should return GET request in response body`() {
      runBlocking {
        val result = client.get("/echo").send().await()

        expectThat(result.bodyAsJsonObject()) {
          get { getString("method") }.isEqualTo("GET")

          and {
            get { getJsonObject("headers").getString("user-agent") }.contains("Vert.x-WebClient")
            get { getJsonObject("headers").getString("host") }.isEqualTo("localhost:8888")
          }
        }
      }
    }

    @Test
    fun `should return GET request with query string in response body`() {
      runBlocking {
        val result = client.get("/echo")
          .addQueryParam("param1", "value1")
          .addQueryParam("param2", "value2")
          .send().await()

        expectThat(result.bodyAsJsonObject()) {
          get { getJsonObject("query_params").getString("param1") }.isEqualTo("value1")
          get { getJsonObject("query_params").getString("param2") }.isEqualTo("value2")
        }
      }
    }

    @Test
    fun `should return POST request in response body`() {
      runBlocking {
        val body = json {
          obj(
            "message" to "hello!",
            "attribute" to "value"
          )
        }

        val result = client.post("/echo")
          .sendJsonObject(body)
          .await()

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
      }
    }
  }
}
