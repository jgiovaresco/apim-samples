package io.apim.samples.rest

import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.impl.ParsableMIMEValue
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.core.MultiMap
import io.vertx.rxjava3.ext.web.ParsedHeaderValues
import io.vertx.rxjava3.ext.web.RequestBody
import io.vertx.rxjava3.ext.web.RoutingContext

fun echoHandler(ctx: RoutingContext) {
  var body: JsonObject
  val response = ctx.response()
    .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")

  try {
    body = json {
      obj(
        "method" to ctx.request().method().name(),
        "headers" to obj(ctx.request().headers().toSimpleMap()),
        "query_params" to obj(ctx.request().params().toSimpleMap()),
        "body" to handleBody(ctx.body(), ctx.parsedHeaders())
      )
    }
    response.statusCode = 200
  } catch (e: DecodeException) {
    response.statusCode = 400
    body = json {
      obj(
        "title" to "The request body fail to be parsed",
        "detail" to e.cause?.message
      )
    }
  }

  response.end(body.toString()).subscribe()
}

fun handleBody(body: RequestBody, headers: ParsedHeaderValues): JsonObject {
  val contentType = (headers.delegate.contentType() as ParsableMIMEValue).forceParse()

    if (contentType.isText()) {
      return json {
        obj(
          "type" to "text",
          "content" to body.asString()
        )
      }
    }

    if (contentType.isJson()) {
      return json {
        obj(
          "type" to "json",
          "content" to body.asJsonObject()
        )
      }
    }

  return json {
    obj(
      "type" to "unknown",
      "content" to body.asString()
    )
  }
}

/** Transform a MultiMap into a simple map. Multiple values are joined in a string separated with ; */
fun MultiMap.toSimpleMap() = this.entries()
  .groupBy { it.key }
  .mapValues { it.value.joinToString(";") { h -> h.value } }

fun ParsableMIMEValue.isText(): Boolean {
  return this.component() == "text"
}

fun ParsableMIMEValue.isJson(): Boolean {
  return this.component() == "application" && this.subComponent().contains("json")
}
