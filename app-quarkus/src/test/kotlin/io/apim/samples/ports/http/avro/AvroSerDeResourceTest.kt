package io.apim.samples.ports.http.avro

import io.apim.samples.core.avro.SerDeFactory
import io.apim.samples.core.avro.SerializationFormat
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.LogConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.LogDetail
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.net.URL

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvroSerDeResourceTest {
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

  @TestHTTPEndpoint(AvroSerDeResource::class)
  @TestHTTPResource
  lateinit var url: URL

  @Inject
  lateinit var serdeFactory: SerDeFactory

  val requestSpecification = RequestSpecBuilder()
    .setContentType(ContentType.JSON)
    .setRelaxedHTTPSValidation()
    .setConfig(
      RestAssuredConfig.config()
        .logConfig(
          LogConfig.logConfig()
            .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
        )
    )
    .build()

  @BeforeEach
  fun setUp() {
    requestSpecification.baseUri(url.toString())
  }

  @ParameterizedTest
  @EnumSource(SerializationFormat::class)
  fun `should return a serialized avro from a json body`(format: SerializationFormat) {
    val serde = serdeFactory.newAvroSerDe(Schema.Parser().parse(schema), format)
    val json = json { obj("id" to "an-id", "amount" to 10.0) }

    val result = Given {
      spec(requestSpecification)
      queryParam("format", format.name)
      header("X-Avro-Schema", schema)
      contentType(MediaType.APPLICATION_JSON)
      body(json.encode())
    } When {
      post()
    } Then {
      statusCode(200)
      contentType("avro/binary")
    } Extract {
      body().asByteArray()
    }

    val data = serde.deserialize(result)

    expectThat(data).isNotNull().isA<GenericRecord>().and {
      get { get("id") }.isNotNull().isA<Utf8>()
      get { get("amount") }.isNotNull().isA<Double>()
    }
  }

  @ParameterizedTest
  @EnumSource(SerializationFormat::class)
  fun `should return a json from an avro body`(format: SerializationFormat) {
    val serde = serdeFactory.newAvroSerDe(Schema.Parser().parse(schema), format)
    val datum = GenericData.Record(Schema.Parser().parse(schema)).apply {
      put("id", "an-id")
      put("amount", 10.0)
    }


    val result = Given {
      spec(requestSpecification)
      queryParam("format", format.name)
      header("X-Avro-Schema", schema)
      contentType("avro/binary")
      body(serde.serialize(datum))
    } When {
      post()
    } Then {
      statusCode(200)
      contentType(ContentType.JSON)
    } Extract {
      body().asString()
    }

    expectThat(JsonObject(result)) {
      get { getString("id") }.isNotNull().isA<String>()
      get { getDouble("amount") }.isNotNull().isA<Double>()
    }

  }

  @Test
  fun `should return an error when no schema is provided`() {
    val json = json { obj("id" to "an-id", "amount" to 10.0) }

    val result = Given {
      spec(requestSpecification)
      body(json.encode())
    } When {
      post()
    } Then {
      statusCode(400)
      contentType(ContentType.JSON)
    } Extract {
      body().asString()
    }

    expectThat(JsonObject(result)).and {
      get { getString("title") }.isEqualTo("Avro schema required in X-Avro-Schema header")
    }
  }

  @Test
  fun `should return an error when schema is invalid`() {
    val json = json { obj("id" to "an-id", "amount" to 10.0) }

    val result = Given {
      spec(requestSpecification)
      header("X-Avro-Schema", """{  "type  } """.trimIndent())
      body(json.encode())
    } When {
      post()
    } Then {
      statusCode(400)
      contentType(ContentType.JSON)
    } Extract {
      body().asString()
    }

    expectThat(JsonObject(result)).and {
      get { getString("title") }.isEqualTo("Invalid avro schema")
    }
  }

  @Test
  fun `should return an error when incorrect serialization format`() {
    val json = json { obj("id" to "an-id", "amount" to 10.0) }

    val result = Given {
      spec(requestSpecification)
      header("X-Avro-Schema", schema)
      queryParam("format", "unsupported")
      body(json.encode())
    } When {
      post()
    } Then {
      statusCode(400)
      contentType(ContentType.JSON)
    } Extract {
      body().asString()
    }

    expectThat(JsonObject(result)).and {
      get { getString("title") }.isEqualTo("Invalid format")
      get { getString("detail") }.isEqualTo("Valid values are: confluent, simple")
    }
  }
}
