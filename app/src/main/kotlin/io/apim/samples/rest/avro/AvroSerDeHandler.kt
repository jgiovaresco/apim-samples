package io.apim.samples.rest.avro

import io.apim.samples.avro.JsonSerDe
import io.apim.samples.rest.isAvro
import io.apim.samples.rest.isJson
import io.apim.samples.rest.sendError
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.impl.ParsableMIMEValue
import io.vertx.rxjava3.core.buffer.Buffer
import io.vertx.rxjava3.ext.web.RoutingContext

fun avroSerDeHandler(ctx: RoutingContext) {
  val contentType = (ctx.parsedHeaders().delegate.contentType() as ParsableMIMEValue).forceParse()

  val schema = ctx.getSchemaFromHeader("X-Avro-Schema")
  val jsonSerDe = JsonSerDe(schema)
  val avroSerDe = serdeFactory.new(schema, ctx.getSerializationFormatFromQueryParam())

  if(contentType.isJson()) {
    val data = jsonSerDe.deserialize(ctx.body().asString())
    ctx.response().statusCode = 200
    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "avro/binary")
    ctx.end(Buffer.buffer(avroSerDe.serialize(data))).subscribe()
    return
  }

  if(contentType.isAvro()) {
    val data = avroSerDe.deserialize(ctx.body().buffer().bytes)
    ctx.response().statusCode = 200
    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    ctx.end(Buffer.buffer(jsonSerDe.serialize(data))).subscribe()
    return
  }

  ctx.sendError(400, "Unsupported content type")
  return
}
