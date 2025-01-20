package io.apim.samples.ports.http

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.mutiny.core.Vertx
import io.vertx.mutiny.core.buffer.Buffer
import io.vertx.mutiny.ext.web.client.WebClient
import jakarta.inject.Inject
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.net.URL


@QuarkusTest
class EchoResourceTest {
  @Inject
  lateinit var vertx: Vertx

  @TestHTTPEndpoint(EchoResource::class)
  @TestHTTPResource
  lateinit var url: URL

  lateinit var client: WebClient

  @BeforeEach
  fun setUp() {
    client = WebClient.create(
      vertx,
      WebClientOptions()
        .setDefaultHost(url.host)
        .setDefaultPort(url.port)
    )
  }

  @TestFactory
  fun `Request without body and without query params`() = listOf(
    HttpMethod.GET,
    HttpMethod.DELETE,
    HttpMethod.OPTIONS,
  ).map { method ->
    DynamicTest.dynamicTest("should return $method request in response body") {
      val response = client
        .request(method, url.path)
        .send()
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(response) {
        get { statusCode() }.describedAs("statusCode").isEqualTo(200)
        get { bodyAsJsonObject() }.describedAs("body").isNotNull().and {
          get { getString("method") }.isEqualTo(method.name())

          get { getJsonObject("headers") }.and {
            get { getString(HttpHeaders.USER_AGENT.lowercase()) }.contains("Vert.x-WebClient")
            get { getString(HttpHeaders.HOST.lowercase()) }.isEqualTo("${url.host}:${url.port}")
          }
        }
      }
    }
  }

  @TestFactory
  fun `Request without body and with query params`() = listOf(
    HttpMethod.GET,
    HttpMethod.DELETE,
    HttpMethod.OPTIONS,
  ).map { method ->
    DynamicTest.dynamicTest("should return $method request with query string in response body") {
      val response = client
        .request(method, url.path)
        .addQueryParam("param1", "value1")
        .addQueryParam("param2", "value2")
        .send()
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(response) {
        get { statusCode() }.describedAs("statusCode").isEqualTo(200)
        get { bodyAsJsonObject() }.describedAs("body").isNotNull().and {
          get { getString("method") }.isEqualTo(method.name())

          get { getJsonObject("query_params") }.and {
            get { getString("param1") }.isEqualTo("value1")
            get { getString("param2") }.isEqualTo("value2")
          }
        }
      }
    }
  }

  @TestFactory
  fun `Request with json body`() = listOf(
    HttpMethod.POST to MediaType.APPLICATION_JSON,
    HttpMethod.POST to "application/vnd.company.api-v1+json",
    HttpMethod.PUT to MediaType.APPLICATION_JSON,
    HttpMethod.PUT to "application/vnd.company.api-v1+json",
  ).map { (method, contentType) ->
    DynamicTest.dynamicTest("should return $method request with '$contentType' body in response") {
      val body = json {
        obj(
          "message" to "hello!",
          "attribute" to "value"
        )
      }

      val response = client
        .request(method, url.path)
        .putHeader(HttpHeaders.CONTENT_TYPE, contentType)
        .sendJsonObject(body)
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(response) {
        get { statusCode() }.describedAs("statusCode").isEqualTo(200)
        get { bodyAsJsonObject() }.describedAs("body").isNotNull().and {
          get { getString("method") }.isEqualTo(method.name())

          get { getJsonObject("headers") }.and {
            get { getString(HttpHeaders.USER_AGENT.lowercase()) }.contains("Vert.x-WebClient")
            get { getString(HttpHeaders.HOST.lowercase()) }.isEqualTo("${url.host}:${url.port}")
            get { getString(HttpHeaders.CONTENT_TYPE.lowercase()) }.isEqualTo(contentType)
            get { getString(HttpHeaders.CONTENT_LENGTH.lowercase()) }.isEqualTo(body.toString().length.toString())
          }

          get { getJsonObject("body") }.and {
            get { getString("type") }.isEqualTo("json")
            get { getJsonObject("content") }.isEqualTo(body)
          }
        }
      }
    }
  }

  @TestFactory
  fun `Request with text body`() = listOf(
    HttpMethod.POST to "text/plain",
    HttpMethod.POST to "text/html",
    HttpMethod.POST to "text/xml",
    HttpMethod.PUT to "text/plain",
    HttpMethod.PUT to "text/html",
    HttpMethod.PUT to "text/xml",
  ).map { (method, contentType) ->
    DynamicTest.dynamicTest("should return $method request with '$contentType' body in response") {
      val body = "a random text"

      val response = client
        .request(method, url.path)
        .putHeader(HttpHeaders.CONTENT_TYPE, contentType)
        .sendBuffer(Buffer.buffer(body))
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(response) {
        get { statusCode() }.describedAs("statusCode").isEqualTo(200)
        get { bodyAsJsonObject() }.describedAs("body").isNotNull().and {
          get { getString("method") }.isEqualTo(method.name())

          get { getJsonObject("headers") }.and {
            get { getString(HttpHeaders.USER_AGENT.lowercase()) }.contains("Vert.x-WebClient")
            get { getString(HttpHeaders.HOST.lowercase()) }.isEqualTo("${url.host}:${url.port}")
            get { getString(HttpHeaders.CONTENT_TYPE.lowercase()) }.isEqualTo(contentType)
            get { getString(HttpHeaders.CONTENT_LENGTH.lowercase()) }.isEqualTo(body.length.toString())
          }

          get { getJsonObject("body") }.and {
            get { getString("type") }.isEqualTo("text")
            get { getString("content") }.isEqualTo(body)
          }
        }
      }
    }
  }

  @TestFactory
  fun `Request with unknown body`() = listOf(
    HttpMethod.POST,
    HttpMethod.PUT,
  ).map { method ->
    DynamicTest.dynamicTest("should return $method request with unknown body in response") {
      val body = "a random text"

      val response = client
        .request(method, url.path)
        .sendBuffer(Buffer.buffer(body))
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(response) {
        get { statusCode() }.describedAs("statusCode").isEqualTo(200)
        get { bodyAsJsonObject() }.describedAs("body").isNotNull().and {
          get { getString("method") }.isEqualTo(method.name())

          get { getJsonObject("headers") }.and {
            get { getString(HttpHeaders.USER_AGENT.lowercase()) }.contains("Vert.x-WebClient")
            get { getString(HttpHeaders.HOST.lowercase()) }.isEqualTo("${url.host}:${url.port}")
            get { getString(HttpHeaders.CONTENT_TYPE.lowercase()) }.isNull()
            get { getString(HttpHeaders.CONTENT_LENGTH.lowercase()) }.isEqualTo(body.length.toString())
          }

          get { getJsonObject("body") }.and {
            get { getString("type") }.isEqualTo("unknown")
            get { getString("content") }.isEqualTo(body)
          }
        }
      }
    }
  }

  @TestFactory
  fun `Request with malformed body`() = listOf(
    HttpMethod.POST,
    HttpMethod.PUT,
  ).map { method ->
    DynamicTest.dynamicTest("should return bad request when $method request with malformed body") {
      val body = "a message"

      val response = client
        .request(method, url.path)
        .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        .sendBuffer(Buffer.buffer(body))
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(response) {
        get { statusCode() }.describedAs("statusCode").isEqualTo(400)
        get { bodyAsJsonObject() }.describedAs("body").isNotNull().and {
          get { getString("title") }.isEqualTo("The request body fail to be parsed")
          get { getString("detail") }.contains("Unrecognized token 'a': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')")
        }
      }
    }
  }


  @TestFactory
  fun `Override response status code`() = listOf(
    HttpMethod.GET,
    HttpMethod.DELETE,
    HttpMethod.OPTIONS,
    HttpMethod.PUT,
    HttpMethod.POST,
  ).map { method ->
    DynamicTest.dynamicTest("should override status code from query param for $method request") {
      val response = client
        .request(method, url.path)
        .addQueryParam("statusCode", "201")
        .sendBuffer(Buffer.buffer("message"))
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(response) {
        get { statusCode() }.describedAs("statusCode").isEqualTo(201)
      }
    }

    DynamicTest.dynamicTest("should override status code from header param for $method request") {
      val response = client
        .request(method, url.path)
        .putHeader("X-Override-Status-Code", "202")
        .sendBuffer(Buffer.buffer("message"))
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(response) {
        get { statusCode() }.describedAs("statusCode").isEqualTo(202)
      }
    }

    DynamicTest.dynamicTest("should fallback to 200 status code when overridden status is incorrect for $method request") {
      val response = client
        .request(method, url.path)
        .addQueryParam("statusCode", "unknown")
        .putHeader("X-Override-Status-Code", "other")
        .sendBuffer(Buffer.buffer("message"))
        .subscribe()
        .withSubscriber(UniAssertSubscriber.create())
        .awaitItem()
        .item

      expectThat(response) {
        get { statusCode() }.describedAs("statusCode").isEqualTo(200)
      }
    }
  }
}
