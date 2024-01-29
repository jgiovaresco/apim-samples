package io.apim.samples.ports.graphql

import io.apim.samples.core.starwars.Film
import io.apim.samples.core.starwars.Person
import io.apim.samples.core.starwars.Planet
import io.apim.samples.core.starwars.StarWarsQueryService
import jakarta.inject.Inject
import org.eclipse.microprofile.graphql.*

@GraphQLApi
class StarWarsResource {
  @Inject
  lateinit var starWarsQueryService: StarWarsQueryService

  @Query("allFilms")
  @Description("Get all films")
  fun getAllFilms(@Name("limit") limit: Int?): List<Film> {
    return starWarsQueryService.getFilms(limit)
  }

  @Query
  @Description("Get a film by id")
  fun getFilm(@Name("filmId") id: Int): Film? = starWarsQueryService.getFilm(id)

  @Query("allPlanets")
  @Description("Get all planets")
  fun getAllPlanets(@Name("limit") limit: Int?): List<Planet> = starWarsQueryService.getPlanets(limit)

  @Query
  @Description("Get a planet by id")
  fun getPlanet(@Name("planetId") id: Int): Planet? = starWarsQueryService.getPlanet(id)

  @Query("allPeople")
  @Description("Get all people")
  fun getAllPeople(@Name("limit") limit: Int?): List<Person> = starWarsQueryService.getPeople(limit)

  @Query
  @Description("Get a person by id")
  fun getPerson(@Name("personId") id: Int): Person? = starWarsQueryService.getPerson(id)

  fun people(@Source film: Film): List<Person> = starWarsQueryService.getPersonByFilm(film)

  @JvmName("getPersonByFilms")
  fun people(@Source films: List<Film>): List<List<Person>> = films.map { starWarsQueryService.getPersonByFilm(it) }

  fun people(@Source planet: Planet): List<Person> = starWarsQueryService.getPersonByPlanet(planet)

  @JvmName("getPersonByPlanets")
  fun people(@Source planets: List<Planet>): List<List<Person>> = planets.map { starWarsQueryService.getPersonByPlanet(it) }
}
