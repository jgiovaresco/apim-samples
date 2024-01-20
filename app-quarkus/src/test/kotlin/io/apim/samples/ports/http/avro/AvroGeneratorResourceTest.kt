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
import jakarta.inject.Inject
import jakarta.ws.rs.core.MediaType
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import strikt.api.expectThat
import strikt.assertions.*
import java.net.URL

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AvroGeneratorResourceTest {
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

  @TestHTTPEndpoint(AvroGeneratorResource::class)
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
  fun `should return a serialized avro`(format: SerializationFormat) {
    val serde = serdeFactory.newAvroSerDe(Schema.Parser().parse(schema), format)

    val result = Given {
      spec(requestSpecification)
      queryParam("format", format.name)
      body(schema)
    } When {
      post()
    } Then {
      statusCode(200)
      contentType("application/*+avro")
    } Extract {
      body().asByteArray()
    }

    val data = serde.deserialize(result)

    expectThat(data).isNotNull().isA<GenericRecord>().and {
      get { get("id") }.isNotNull().isA<Utf8>()
      get { get("amount") }.isNotNull().isA<Double>()
    }
  }

  @Test
  fun `should return a json matching the schema provided`() {
    val result = Given {
      spec(requestSpecification)
      queryParam("output", "json")
      body(schema)
    } When {
      post()
    } Then {
      statusCode(200)
      contentType(MediaType.APPLICATION_JSON)
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
    val result = Given {
      spec(requestSpecification)
      body("")
    } When {
      post()
    } Then {
      statusCode(400)
      contentType(ContentType.JSON)
    } Extract {
      body().asString()
    }

    expectThat(JsonObject(result)).and {
      get { getString("title") }.isEqualTo("Provide an avro schema")
    }
  }

  @Test
  fun `should return an error when schema is invalid`() {
    val result = Given {
      spec(requestSpecification)
      body("""{  "type  } """.trimIndent())
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
  fun `should return an error when output format is not supported`() {
    val result = Given {
      spec(requestSpecification)
      queryParam("output", "unsupported")
      body(schema)
    } When {
      post()
    } Then {
      statusCode(400)
      contentType(ContentType.JSON)
    } Extract {
      body().asString()
    }

    expectThat(JsonObject(result)).and {
      get { getString("title") }.isEqualTo("Invalid output format")
      get { getString("detail") }.isEqualTo("Valid values are: avro, json")
    }
  }

  @Test
  fun `should return an error when format is not supported`() {
    val result = Given {
      spec(requestSpecification)
      queryParam("format", "unsupported")
      body(schema)
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
