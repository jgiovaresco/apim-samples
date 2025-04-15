package io.apim.samples.ports.sse

import io.apim.samples.core.quote.QuoteRecord
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder
import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.helpers.test.AssertSubscriber
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient
import org.jboss.resteasy.reactive.RestQuery
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.all
import strikt.assertions.hasSize
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull
import java.net.URL
import java.time.Duration


@QuarkusTest
class SSEEndpointTest {

  @RegisterRestClient
  interface SseClient {
    @GET
    @Path("quotes")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    fun quotes(@RestQuery delayInMs: Long?, @RestQuery nbMessages: Long?): Multi<QuoteRecord>?
  }

  lateinit var sseClient: SseClient

  @TestHTTPEndpoint(SSEEndpoint::class)
  @TestHTTPResource
  lateinit var url: URL

  @BeforeEach
  fun setUp() {
    sseClient = QuarkusRestClientBuilder.newBuilder()
      .baseUri(url.toURI())
      .build(SseClient::class.java);
  }

  @Test
  fun `get quotes with default configuration`() {
    val items = sseClient.quotes(null ,null)
      ?.select()?.first(Duration.ofSeconds(2))
      ?.subscribe()
      ?.withSubscriber(AssertSubscriber.create(2))
      ?.awaitItems(2)
      ?.items

    expectThat(items).isNotNull().and {
      hasSize(2)

      all {
        get { title }.isNotEmpty()
        get { quote }.isNotEmpty()
      }
    }
  }

  @Test
  fun `get quotes with custom configuration`() {
    val items = sseClient.quotes(500 ,4)
      ?.subscribe()
      ?.withSubscriber(AssertSubscriber.create(4))
      ?.awaitItems(4)
      ?.items

    expectThat(items).isNotNull().and {
      hasSize(4)

      all {
        get { title }.isNotEmpty()
        get { quote }.isNotEmpty()
      }
    }
  }
}
