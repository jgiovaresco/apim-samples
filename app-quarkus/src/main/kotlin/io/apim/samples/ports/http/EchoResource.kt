package io.apim.samples.ports.http

import io.apim.samples.core.isJson
import io.apim.samples.core.isText
import io.apim.samples.core.toSimpleMap
import io.quarkus.vertx.web.Body
import io.quarkus.vertx.web.Route
import io.quarkus.vertx.web.RouteBase
import io.quarkus.vertx.web.RoutingExchange
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.impl.ParsableMIMEValue
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.HttpHeaders
import jakarta.ws.rs.core.MediaType
import kotlin.jvm.optionals.getOrNull

@RouteBase(path = "/echo")
class EchoResource {

  @Route(methods = [Route.HttpMethod.GET, Route.HttpMethod.DELETE, Route.HttpMethod.HEAD, Route.HttpMethod.OPTIONS], path = "", produces = [MediaType.APPLICATION_JSON], order = 1)
  @Produces(MediaType.APPLICATION_JSON)
  fun withoutBody(ctx: RoutingExchange): JsonObject {
    val delay = extractDelay(ctx)
    if (delay > 0) {
      Thread.sleep(delay)
    }
    ctx.response().statusCode = extractStatusCode(ctx)
    return json {
      obj(initResponseBody(ctx))
    }
  }

  @Route(methods = [Route.HttpMethod.POST, Route.HttpMethod.PUT], path = "", produces = [MediaType.APPLICATION_JSON], order = 2)
  fun withBody(@Body requestBody: Buffer, ctx: RoutingExchange): JsonObject {
    val delay = extractDelay(ctx)
    if (delay > 0) {
      Thread.sleep(delay)
    }
    val contentType = ctx.request().getHeader(HttpHeaders.CONTENT_TYPE)?.let { ParsableMIMEValue(it).forceParse() }
    val (type, content) = readBody(contentType, requestBody)

    ctx.response().statusCode = extractStatusCode(ctx)
    return json {
      obj(initResponseBody(ctx))
        .put("body", json { obj("type" to type, "content" to content) })
    }
  }

  @Route(type = Route.HandlerType.FAILURE, produces = [MediaType.APPLICATION_JSON], order = 3)
  fun exception(e: DecodeException, response: HttpServerResponse) {
    response.setStatusCode(400).end(
      json {
        obj(
          "title" to "The request body fail to be parsed",
          "detail" to e.cause?.message
        )
      }.encode()
    )
  }
  private fun extractStatusCode(ctx: RoutingExchange): Int {
    return ctx.getParam("statusCode")
      .or { ctx.getHeader("X-Override-Status-Code") }
      .getOrNull()
      ?.toIntOrNull()
      ?: 200
  }

  private fun extractDelay(ctx: RoutingExchange): Long {
    return ctx.getHeader("X-Delay")
      .or { ctx.getParam("delay") }
      .getOrNull()
      ?.toLongOrNull()
      ?: 0
  }

  private fun initResponseBody(ctx: RoutingExchange) = mutableMapOf(
    "method" to ctx.request().method().name(),
    "headers" to json { obj(ctx.request().headers().toSimpleMap()) },
    "query_params" to json { obj(ctx.request().params().toSimpleMap()) },
  )


  private fun readBody(contentType: ParsableMIMEValue?, body: Buffer): Pair<String, Any> {
    if (contentType == null) {
      return "unknown" to body.toString()
    }

    if (contentType.isJson()) {
      return "json" to body.toJsonObject()
    }

    if (contentType.isText()) {
      return "text" to body.toString()
    }

    return "unknown" to body.toString()
  }
}
