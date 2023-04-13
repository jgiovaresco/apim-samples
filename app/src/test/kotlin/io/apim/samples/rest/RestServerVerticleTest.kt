package io.apim.samples.rest

import io.apim.samples.avro.AvroSerDeFactoryImpl
import io.apim.samples.avro.SerializationFormat
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.config.ConfigRetriever
import io.vertx.rxjava3.core.Vertx
import io.vertx.rxjava3.core.buffer.Buffer
import io.vertx.rxjava3.ext.web.client.WebClient
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.io.path.Path

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

    @Nested
    inner class PostRequest {
      @ParameterizedTest
      @ValueSource(
        strings = [
          "application/json",
          "application/vnd.company.api-v1+json",
        ]
      )
      fun `should return json request in response body`(contentType: String) {
        val body = json {
          obj(
            "message" to "hello!",
            "attribute" to "value"
          )
        }

        client.post("/echo")
          .putHeader(HttpHeaders.CONTENT_TYPE.toString(), contentType)
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
                get { getJsonObject("headers").getString("content-type") }.isEqualTo(contentType)
                get { getJsonObject("headers").getString("content-length") }.isEqualTo(body.toString().length.toString())
              }

              and {
                get { getJsonObject("body").getString("type") }.isEqualTo("json")
                get { getJsonObject("body").getJsonObject("content") }.isEqualTo(body)
              }
            }
            true
          }
      }

      @ParameterizedTest
      @ValueSource(
        strings = [
          "text/plain",
          "text/html",
          "text/xml"
        ]
      )
      fun `should return text request in response body`(contentType: String) {
        val body = "a random text"

        client.post("/echo")
          .putHeader(HttpHeaders.CONTENT_TYPE.toString(), contentType)
          .sendBuffer(Buffer.buffer(body))
          .test()
          .await()
          .assertNoErrors()
          .assertValue { result ->
            expectThat(result.bodyAsJsonObject()) {
              get { getString("method") }.isEqualTo("POST")

              and {
                get { getJsonObject("headers").getString("user-agent") }.contains("Vert.x-WebClient")
                get { getJsonObject("headers").getString("host") }.isEqualTo("localhost:8888")
                get { getJsonObject("headers").getString("content-type") }.isEqualTo(contentType)
                get { getJsonObject("headers").getString("content-length") }.isEqualTo(body.length.toString())
              }

              and {
                get { getJsonObject("body").getString("type") }.isEqualTo("text")
                get { getJsonObject("body").getString("content") }.isEqualTo(body)
              }
            }
            true
          }
      }

      @Test
      fun `should return unknown type body request in response body`() {
        val body = "unknown"

        client.post("/echo")
          .sendBuffer(Buffer.buffer(body))
          .test()
          .await()
          .assertNoErrors()
          .assertValue { result ->
            expectThat(result.bodyAsJsonObject()) {
              get { getString("method") }.isEqualTo("POST")

              and {
                get { getJsonObject("headers").getString("user-agent") }.contains("Vert.x-WebClient")
                get { getJsonObject("headers").getString("host") }.isEqualTo("localhost:8888")
                get { getJsonObject("headers").getString("content-type") }.isNull()
                get { getJsonObject("headers").getString("content-length") }.isEqualTo(body.length.toString())
              }

              and {
                get { getJsonObject("body").getString("type") }.isEqualTo("unknown")
                get { getJsonObject("body").getString("content") }.isEqualTo(body)
              }
            }
            true
          }
      }

      @Test
      fun `should return a bad request error when malformed Json request`() {
        val body = "a message"

        client.post("/echo")
          .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
          .sendBuffer(Buffer.buffer(body))
          .test()
          .await()
          .assertNoErrors()
          .assertValue { result ->
            expectThat(result.bodyAsJsonObject()) {
              get { getString("title") }.isEqualTo("The request body fail to be parsed")
              get { getString("detail") }.contains("Unrecognized token 'a': was expecting (JSON String, Number, Array, Object or token 'null', 'true' or 'false')")
            }
            true
          }
      }
    }
  }

  @Nested
  @ExtendWith(VertxExtension::class)
  inner class ProtobufFileHandler {
    @Test
    fun `should list all available protobuf files`() {
      client.get("/grpc").send()
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result.bodyAsJsonObject()) {
            get { getJsonArray("protoFiles").list }.containsExactly("http://localhost:8888/grpc/route_guide.proto")
          }
          true
        }
    }

    @Test
    fun `should return the content of a specific proto file`() {
      client.get("/grpc/route_guide.proto").send()
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result.bodyAsString()).isEqualTo(
            Path(ClassLoader.getSystemResource("grpc/route_guide.proto").file).toFile().readText()
          )
          true
        }
    }

    @ParameterizedTest
    @ValueSource(strings = [ "unknown", "unknown.proto" ])
    fun `should return 404 when the requested file does not exist`(file: String) {
      client.get("/grpc/$file").send()
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result.statusCode()).isEqualTo(404)
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
            get { statusCode() }.isEqualTo(204)
          }
          true
        }
    }
  }

  @Nested
  @ExtendWith(VertxExtension::class)
  inner class AvroGeneratorHandler {
    private val schema = """
      {
        "type": "record",
        "name": "Payment",
        "fields": [
            {
                "name": "id",
                "type": "string"
            },
            {
                "name": "amount",
                "type": "double"
            }
        ]
      }
      """.trimIndent()

    @ParameterizedTest
    @EnumSource(SerializationFormat::class)
    fun `should return a serialized avro`(format: SerializationFormat) {
      val serde = AvroSerDeFactoryImpl().new(Schema.Parser().parse(schema), format)

      client.post("/avro/generate")
        .addQueryParam("format", format.name)
        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
        .sendBuffer(Buffer.buffer(schema))
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->

          val avro = result.bodyAsBuffer().bytes
          val data = serde.deserialize(avro)

          expectThat(data).isNotNull().isA<GenericRecord>().and {
            get { get("id") }.isNotNull().isA<Utf8>()
            get { get("amount") }.isNotNull().isA<Double>()
          }

          true
        }
    }

    @Test
    fun `should return a json matching schema provided`() {
      client.post("/avro/generate")
        .addQueryParam("output", "json")
        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
        .sendBuffer(Buffer.buffer(schema))
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->

          val json = result.bodyAsJsonObject()

          expectThat(json).isNotNull().and {
            get { getString("id") }.isNotNull().isA<String>()
            get { getDouble("amount") }.isNotNull().isA<Double>()
          }

          true
        }
    }

    @Test
    fun `should return an error when no schema is provided`() {
      client.post("/avro/generate")
        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
        .sendBuffer(Buffer.buffer())
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result) {
            get { statusCode() }.isEqualTo(400)
            get { bodyAsJsonObject() }.and {
              get { getString("title") }.isEqualTo("Provide an avro schema")
            }
          }
          true
        }
    }

    @Test
    fun `should return an error when schema is invalid`() {
      client.post("/avro/generate")
        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
        .sendBuffer(Buffer.buffer("""{  "type  } """.trimIndent()))
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result) {
            get { statusCode() }.isEqualTo(400)
            get { bodyAsJsonObject() }.and {
              get { getString("title") }.isEqualTo("Invalid avro schema")
            }
          }
          true
        }
    }

    @Test
    fun `should return an error when output format is not supported`() {
      client.post("/avro/generate")
        .addQueryParam("output", "unknown")
        .putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
        .sendBuffer(Buffer.buffer(schema))
        .test()
        .await()
        .assertNoErrors()
        .assertValue { result ->
          expectThat(result) {
            get { statusCode() }.isEqualTo(400)
            get { bodyAsJsonObject() }.and {
              get { getString("title") }.isEqualTo("Invalid output format")
              get { getString("detail") }.isEqualTo("Valid values are: avro, json")
            }
          }
          true
        }
    }
  }
}
