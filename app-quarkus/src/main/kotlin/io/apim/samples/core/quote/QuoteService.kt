package io.apim.samples.core.quote

import io.github.serpro69.kfaker.Faker
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class QuoteService {
  private val faker = Faker()

  private val quotes: Map<String, () -> String> = mapOf(
    "bigBangTheory" to { faker.bigBangTheory.quotes() },
    "brooklynNineNine" to { faker.brooklynNineNine.quotes() },
    "friends" to { faker.friends.quotes() },
    "howIMetYourMother" to { faker.howIMetYourMother.quote() },
    "rickAndMorty" to { faker.rickAndMorty.quotes() },
    "theITCrowd" to { faker.theITCrowd.quotes() },
    "theOffice" to { faker.theOffice.quotes() },
  )

  fun randomQuote(): QuoteRecord? {
    val title =  quotes.keys.random()
    val quote: (() -> String)? = quotes[title]

    if (quote != null) {
      return QuoteRecord(title, quote())
    }
    return null

  }
}

data class QuoteRecord(val title: String, val quote: String)
