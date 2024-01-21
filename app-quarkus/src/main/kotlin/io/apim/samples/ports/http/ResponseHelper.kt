package io.apim.samples.ports.http

import io.vertx.core.http.HttpServerResponse
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType

fun HttpServerResponse.sendError(statusCode: Int, title: String, detail: String? = null) {
  this.statusCode = statusCode
  this.putHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
  this.end(json {
    obj(
      "title" to title,
      "detail" to detail
    )
  }
    .encode())
}
