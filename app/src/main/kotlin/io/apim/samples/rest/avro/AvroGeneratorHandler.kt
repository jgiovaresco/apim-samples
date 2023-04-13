package io.apim.samples.rest.avro

import io.apim.samples.avro.AvroSerDeFactoryImpl
import io.apim.samples.avro.JsonSerDe
import io.apim.samples.avro.SerializationFormat
import io.apim.samples.avro.generate
import io.apim.samples.rest.isJson
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.impl.ParsableMIMEValue
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.core.buffer.Buffer
import io.vertx.rxjava3.ext.web.RoutingContext
import org.apache.avro.Schema
import org.apache.avro.SchemaParseException
import java.util.*

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

private fun getOutputFormat(ctx: RoutingContext): OutputFormat {
  val output = ctx.queryParam("output").elementAtOrNull(0) ?: "avro"
  try {
    return OutputFormat.valueOf(output.uppercase(Locale.getDefault()))
  } catch (e: IllegalArgumentException) {
    ctx.sendError(400, "Invalid output format", "Valid values are: avro, json")
    throw e
  }
}

private fun generate(ctx: RoutingContext) {
  try {
    val output = getOutputFormat(ctx)
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

  val format = ctx.queryParam("format").elementAtOrNull(0)?.let { SerializationFormat.valueOf(it) }
    ?: SerializationFormat.CONFLUENT
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

private fun RoutingContext.sendError(statusCode: Int, title: String, detail: String? = null) {
  this.response().statusCode = statusCode
  this.end(io.vertx.kotlin.core.json.json {
    obj(
      "title" to title,
      "detail" to detail
    )
  }
    .toString()).subscribe()
}
