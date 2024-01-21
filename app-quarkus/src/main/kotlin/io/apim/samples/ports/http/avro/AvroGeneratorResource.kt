package io.apim.samples.ports.http.avro

import io.apim.samples.core.avro.SerDeFactory
import io.apim.samples.core.avro.SerializationFormat
import io.apim.samples.core.avro.impl.JsonSerDe
import io.apim.samples.ports.http.sendError
import io.quarkus.vertx.web.Body
import io.quarkus.vertx.web.Route
import io.quarkus.vertx.web.RouteBase
import io.quarkus.vertx.web.RoutingExchange
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.DecodeException
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.apache.avro.Schema
import org.apache.avro.SchemaParseException
import java.util.*
import kotlin.jvm.optionals.getOrDefault

enum class OutputFormat {
  AVRO, JSON
}

@RouteBase(path = "/avro/generate")
class AvroGeneratorResource(private val serdeFactory: SerDeFactory) {

  @Route(methods = [Route.HttpMethod.POST], path = "", consumes = [MediaType.APPLICATION_JSON], order = 1)
  fun generate(@Body body: Buffer?, ctx: RoutingExchange) {
    if (body == null || body.length() == 0) {
      ctx.response().sendError(Response.Status.BAD_REQUEST.statusCode, "Provide an avro schema")
      return
    }

    try {
      val output = ctx.getOutputFormatFromQueryParam()
      val schema = Schema.Parser().parse(body.toString())

      when (output) {
        OutputFormat.AVRO -> generateAvro(schema, ctx)
        OutputFormat.JSON -> generateJson(schema, ctx)
      }
    } catch (e: SchemaParseException) {
      ctx.response().sendError(Response.Status.BAD_REQUEST.statusCode, "Invalid avro schema", e.message)
      return
    }
  }

  @Route(type = Route.HandlerType.FAILURE, produces = [MediaType.APPLICATION_JSON], order = 2)
  fun exception(e: DecodeException, response: HttpServerResponse) {
    response.setStatusCode(Response.Status.BAD_REQUEST.statusCode).end(
      json {
        obj(
          "title" to "The request body fail to be parsed",
          "detail" to e.cause?.message
        )
      }.encode()
    )
  }

  private fun generateAvro(schema: Schema, ctx: RoutingExchange) {
    val data = io.apim.samples.core.avro.generate(schema)

    val format = ctx.getSerializationFormatFromQueryParam()
    val serde = serdeFactory.newAvroSerDe(schema, format)

    ctx.response().statusCode = Response.Status.OK.statusCode
    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/*+avro")
    ctx.response().end(Buffer.buffer(serde.serialize(data)))
  }

  private fun generateJson(schema: Schema, ctx: RoutingExchange) {
    val data = io.apim.samples.core.avro.generate(schema)
    val serde = JsonSerDe(schema)

    ctx.response().statusCode = Response.Status.OK.statusCode
    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    ctx.response().end(serde.serialize(data))
  }
}

fun RoutingExchange.getOutputFormatFromQueryParam(): OutputFormat {
  val output = getParam("output").getOrDefault("avro")
  try {
    return OutputFormat.valueOf(output.uppercase(Locale.getDefault()))
  } catch (e: IllegalArgumentException) {
    response().sendError(Response.Status.BAD_REQUEST.statusCode, "Invalid output format", "Valid values are: ${OutputFormat.entries.joinToString(", ") { it.name.lowercase() }}")
    throw e
  }
}

fun RoutingExchange.getSerializationFormatFromQueryParam(): SerializationFormat {
  val format = getParam("format").getOrDefault(SerializationFormat.CONFLUENT.name)
  try {
    return SerializationFormat.valueOf(format.uppercase(Locale.getDefault()))
  } catch (e: IllegalArgumentException) {
    response().sendError(Response.Status.BAD_REQUEST.statusCode, "Invalid format", "Valid values are: ${SerializationFormat.entries.joinToString(", ") { it.name.lowercase() }}")
    throw e
  }
}
