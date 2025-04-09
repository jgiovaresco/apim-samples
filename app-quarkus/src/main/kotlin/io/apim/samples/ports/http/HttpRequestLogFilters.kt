package io.apim.samples.ports.http

import io.quarkus.vertx.web.RouteFilter
import io.vertx.ext.web.RoutingContext
import org.jboss.logging.Logger

class HttpRequestLogFilters {
  companion object {
    val LOG = Logger.getLogger(HttpRequestLogFilters::class.java)
  }


  @RouteFilter(100)
  fun logRequest(rc: RoutingContext) {
    LOG.debug("Request received: ${rc.request().method()} ${rc.request().uri()}\n Headers: ${rc.request().headers()}\n Body: ${rc.body().asString()}")
    rc.next()
  }
}
