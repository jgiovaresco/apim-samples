package io.apim.samples.ports.graphql

import io.quarkus.test.junit.QuarkusTest
import io.smallrye.graphql.client.GraphQLClient
import io.smallrye.graphql.client.core.Document
import io.smallrye.graphql.client.core.Field
import io.smallrye.graphql.client.core.Operation
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient
import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@QuarkusTest
class StarWarsResourceTest {

  @Inject
  @GraphQLClient("star-wars")
  lateinit var client: DynamicGraphQLClient

  @Test
  fun `should return all films`() {
    val query = Document.document(
      Operation.operation("allFilms",
        Field.field("allFilms",
          Field.field("title")
        )
      )
    )

    val result = client.executeSync(query)
    val titles = result.data.getJsonArray("allFilms").stream().map { it.asJsonObject().getString("title") }.toList()

    expectThat(titles).isNotNull()
      .containsExactlyInAnyOrder(
        "A New Hope",
        "The Empire Strikes Back",
        "Return of the Jedi",
        "The Phantom Menace",
        "Attack of the Clones",
        "Revenge of the Sith",
        "The Force Awakens",
        "The Last Jedi",
        "The Rise of Skywalker"
      )
  }

  @Test
  fun `should return a film with all people`() {
    val query = """
      query getFilm{
        film(filmId: 1) {
          title
          episode
          people {
              name
          }
        }
      }
     """.trimIndent()

    val result = client.executeSync(query)

    expectThat(result.data.getJsonObject("film")).isNotNull().and {
      get { getString("title") }.isEqualTo("A New Hope")
      get { getInt("episode") }.isEqualTo(4)
      get { getJsonArray("people").stream().map { it.asJsonObject().getString("name") }.toList() }.containsExactlyInAnyOrder("Luke Skywalker", "C-3PO", "R2-D2", "Darth Vader")
    }

  }
}
