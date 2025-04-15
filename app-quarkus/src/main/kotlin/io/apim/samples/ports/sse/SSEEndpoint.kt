package io.apim.samples.ports.sse

import io.apim.samples.core.quote.QuoteRecord
import io.apim.samples.core.quote.QuoteService
import io.smallrye.mutiny.Multi
import io.vertx.core.eventbus.EventBus
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestQuery
import org.jboss.resteasy.reactive.RestStreamElementType
import java.time.Duration

@Path("/sse")
class SSEEndpoint(private val quoteService: QuoteService, private val eventBus: EventBus) {

  @GET
  @Path("quotes")
  @RestStreamElementType(MediaType.APPLICATION_JSON)
  fun quotes(@RestQuery delayInMs: Long?, @RestQuery nbMessages: Long?): Multi<QuoteRecord> {
    return Multi.createFrom().ticks().every(Duration.ofMillis(delayInMs ?: 1000))
      .select().first { i -> nbMessages == null || i < nbMessages }
      .onItem().transform { _ -> quoteService.randomQuote() }
  }
}
