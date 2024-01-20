package io.apim.samples.ports.http.avro

import io.apim.samples.core.avro.SerDeFactory
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

@RouteBase(path = "/avro/serde")
class AvroSerDeResource(private val serdeFactory: SerDeFactory) {

  @Route(methods = [Route.HttpMethod.POST], path = "", consumes = [MediaType.APPLICATION_JSON, "avro/binary"], order = 1)
  fun serde(@Body body: Buffer, ctx: RoutingExchange) {
    val contentTypeHeader = ctx.request().getHeader(jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE)

    val schema = ctx.getSchemaFromHeader("X-Avro-Schema")
    val jsonSerDe = JsonSerDe(schema)
    val avroSerDe = serdeFactory.newAvroSerDe(schema, ctx.getSerializationFormatFromQueryParam())

    if(contentTypeHeader.contains("json", ignoreCase = true)) {
      val data = jsonSerDe.deserialize(body.toString())
      ctx.response().statusCode = Response.Status.OK.statusCode
      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "avro/binary")
      ctx.response().end(Buffer.buffer(avroSerDe.serialize(data)))
      return
    }

    if(contentTypeHeader.contains("avro", ignoreCase = true)) {
      val data = avroSerDe.deserialize(body.bytes)
      ctx.response().statusCode = Response.Status.OK.statusCode
      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
      ctx.response().end(Buffer.buffer(jsonSerDe.serialize(data)))
      return
    }

    ctx.response().sendError(Response.Status.BAD_REQUEST.statusCode, "Unsupported content type")
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
}

fun RoutingExchange.getSchemaFromHeader(header: String = "X-Avro-Schema"): Schema {
  val schemaString = request().getHeader(header)
  if (schemaString == null) {
    response().sendError(Response.Status.BAD_REQUEST.statusCode, "Avro schema required in $header header")
    throw IllegalArgumentException("Avro schema required in $header header")
  }

  return try {
    Schema.Parser().parse(schemaString)
  } catch (e: SchemaParseException) {
    response().sendError(400, "Invalid avro schema", e.message)
    throw e
  }
}
