package io.apim.samples

import io.apim.samples.rest.RestServerVerticle
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.core.Vertx
import io.vertx.rxjava3.ext.web.client.WebClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
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
class MainVerticleTest {

  @Nested
  @ExtendWith(VertxExtension::class)
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  inner class WithDefaultConfiguration {
    private val vertx: Vertx = Vertx.vertx()

    @BeforeAll
    fun setUp(testContext: VertxTestContext) {
      vertx.deployVerticle(MainVerticle())
        .subscribeBy { testContext.completeNow() }
    }

    @AfterAll
    fun tearDown(testContext: VertxTestContext) {
      vertx.close()
        .subscribeBy { testContext.completeNow() }
    }

    @Test
    fun `should start an http server to handle rest request`() {
      WebClient.create(
        vertx,
        WebClientOptions()
          .setDefaultHost("localhost")
          .setDefaultPort(RestServerVerticle.DEFAULT_PORT)
      )
        .get("/health").send()
        .test()
        .await()
        .assertNoErrors()
    }

    @Test
    fun `should start an http server to handle websocket request`() {
      WebClient.create(
        vertx,
        WebClientOptions()
          .setDefaultHost("localhost")
          .setDefaultPort(RestServerVerticle.DEFAULT_PORT)
      )
        .get("/health").send()
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result) {
            get { statusCode() }.isEqualTo(200)
            get { bodyAsJsonObject().getJsonArray("checks").list }
              .contains(json { obj("id" to "websocket", "status" to "UP") }.map)
          }
          true
        }
    }

    @Test
    fun `should start a grpc server`() {
      WebClient.create(
        vertx,
        WebClientOptions()
          .setDefaultHost("localhost")
          .setDefaultPort(RestServerVerticle.DEFAULT_PORT)
      )
        .get("/health").send()
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result) {
            get { statusCode() }.isEqualTo(200)
            get { bodyAsJsonObject().getJsonArray("checks").list }
              .contains(json { obj("id" to "grpc", "status" to "UP") }.map)
          }
          true
        }
    }
  }
}
