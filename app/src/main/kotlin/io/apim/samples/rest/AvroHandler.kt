package io.apim.samples.rest

import io.apim.samples.avro.AvroSerDeFactoryImpl
import io.apim.samples.avro.SerializationFormat
import io.apim.samples.avro.generate
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.impl.ParsableMIMEValue
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.core.buffer.Buffer
import io.vertx.rxjava3.ext.web.RoutingContext
import org.apache.avro.Schema
import org.apache.avro.SchemaParseException

val serdeFactory = AvroSerDeFactoryImpl()

fun avroHandler(ctx: RoutingContext) {
  val contentType = (ctx.parsedHeaders().delegate.contentType() as ParsableMIMEValue).forceParse()

  if (!contentType.isJson()) {
    ctx.sendError(400, "Provide an avro schema")
    return
  }

  try {
    val schema = Schema.Parser().parse(ctx.body().asString())
    val data = generate(schema)

    val format = ctx.queryParam("format").elementAtOrNull(0)?.let { SerializationFormat.valueOf(it) }
      ?: SerializationFormat.CONFLUENT
    val serde = serdeFactory.new(schema, format)

    ctx.response().statusCode = 200
    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/*+avro")
    ctx.end(Buffer.buffer(serde.serialize(data))).subscribe()

  } catch (e: SchemaParseException) {
    ctx.sendError(400, "Invalid avro schema", e.message)
    return
  }
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
