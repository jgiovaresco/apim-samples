package io.apim.samples.rest

import io.vertx.core.MultiMap
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

fun echoHandler(ctx: RoutingContext) {
  ctx.response()
    .setStatusCode(200)
    .putHeader("Content-Type", "application/json")
    .end(
      json {
        obj(
          "method" to ctx.request().method().name(),
          "headers" to obj(ctx.request().headers().toSimpleMap()),
          "query_params" to obj(ctx.request().params().toSimpleMap()),
          "body" to ctx.body().asJsonObject()
        )
      }.toString()
    )
}

/** Transform a multimap into a simple map. Multiple values are joined in a string separated with ; */
fun MultiMap.toSimpleMap() = this.entries()
  .groupBy { it.key }
  .mapValues { it.value.joinToString(";") { h -> h.value } }
