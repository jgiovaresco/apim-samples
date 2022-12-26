package io.apim.samples

import io.apim.samples.rest.RestServerVerticle
import io.apim.samples.websocket.WebSocketServerVerticle
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.rxjava3.core.Vertx
import io.vertx.rxjava3.ext.web.client.WebClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith

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
          .setDefaultPort(WebSocketServerVerticle.DEFAULT_PORT)
      )
        .get("/health").send()
        .test()
        .await()
        .assertNoErrors()
    }
  }
}
