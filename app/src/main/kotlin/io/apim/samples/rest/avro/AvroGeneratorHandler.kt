package io.apim.samples.rest.avro

import io.apim.samples.avro.AvroSerDeFactoryImpl
import io.apim.samples.avro.JsonSerDe
import io.apim.samples.avro.generate
import io.apim.samples.rest.isJson
import io.apim.samples.rest.sendError
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.impl.ParsableMIMEValue
import io.vertx.rxjava3.core.buffer.Buffer
import io.vertx.rxjava3.ext.web.RoutingContext
import org.apache.avro.Schema
import org.apache.avro.SchemaParseException

val serdeFactory = AvroSerDeFactoryImpl()

enum class OutputFormat {
  AVRO, JSON
}

fun avroGeneratorHandler(ctx: RoutingContext) {
  val contentType = (ctx.parsedHeaders().delegate.contentType() as ParsableMIMEValue).forceParse()

  if (!contentType.isJson() || ctx.body().isEmpty) {
    ctx.sendError(400, "Provide an avro schema")
    return
  }

  generate(ctx)
}

private fun generate(ctx: RoutingContext) {
  try {
    val output = ctx.getOutputFormatFromQueryParam()
    val schema = Schema.Parser().parse(ctx.body().asString())

    when (output) {
      OutputFormat.AVRO -> generateAvro(schema, ctx)
      OutputFormat.JSON -> generateJson(schema, ctx)
    }
  } catch (e: SchemaParseException) {
    ctx.sendError(400, "Invalid avro schema", e.message)
    return
  }
}

private fun generateAvro(schema: Schema, ctx: RoutingContext) {
  val data = generate(schema)

  val format = ctx.getSerializationFormatFromQueryParam()
  val serde = serdeFactory.new(schema, format)

  ctx.response().statusCode = 200
  ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/*+avro")
  ctx.end(Buffer.buffer(serde.serialize(data))).subscribe()
}

private fun generateJson(schema: Schema, ctx: RoutingContext) {
  val data = generate(schema)
  val serde = JsonSerDe(schema)

  ctx.response().statusCode = 200
  ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
  ctx.end(serde.serialize(data)).subscribe()
}
