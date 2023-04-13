package io.apim.samples.rest

import io.vertx.kotlin.core.json.obj
import io.vertx.rxjava3.ext.web.RoutingContext

fun RoutingContext.sendError(statusCode: Int, title: String, detail: String? = null) {
  this.response().statusCode = statusCode
  this.end(io.vertx.kotlin.core.json.json {
    obj(
      "title" to title,
      "detail" to detail
    )
  }
    .toString()).subscribe()
}
