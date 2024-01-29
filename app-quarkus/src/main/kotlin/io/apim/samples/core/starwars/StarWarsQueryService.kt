package io.apim.samples.core.starwars

import jakarta.enterprise.context.ApplicationScoped
import java.time.LocalDate

@ApplicationScoped
class StarWarsQueryService {
  private val films = ArrayList<Film>()
  private val planets = ArrayList<Planet>()
  private val vehicles = ArrayList<Vehicle>()
  private val starships = ArrayList<Starship>()
  private val people = ArrayList<Person>()

  init {
    films.add(Film(1, "A New Hope", 4, "George Lucas", LocalDate.of(1977, 5, 25)))
    films.add(Film(2, "The Empire Strikes Back", 5, "Irvin Kershner", LocalDate.of(1980, 5, 21)))
    films.add(Film(3, "Return of the Jedi", 6, "Richard Marquand", LocalDate.of(1983, 5, 25)))
    films.add(Film(4, "The Phantom Menace", 1, "George Lucas", LocalDate.of(1999, 5, 19)))
    films.add(Film(5, "Attack of the Clones", 2, "George Lucas", LocalDate.of(2002, 5, 16)))
    films.add(Film(6, "Revenge of the Sith", 3, "George Lucas", LocalDate.of(2005, 5, 19)))
    films.add(Film(7, "The Force Awakens", 7, "J. J. Abrams", LocalDate.of(2015, 12, 18)))
    films.add(Film(8, "The Last Jedi", 8, "Rian Johnson", LocalDate.of(2017, 12, 15)))
    films.add(Film(9, "The Rise of Skywalker", 9, "J. J. Abrams", LocalDate.of(2019, 12, 20)))

    planets.add(Planet(1, "Tatooine", "arid", "1 standard", 200000, listOf(films[0], films[2], films[3], films[4], films[5])))
    planets.add(Planet(2, "Alderaan", "temperate", "1 standard", 2000000000, listOf(films[0], films[2], films[3])))
    planets.add(Planet(3, "Yavin IV", "temperate, tropical", "1 standard", 1000, listOf(films[0])))
    planets.add(Planet(4, "Hoth", "frozen", "1.1 standard", 2000000000, listOf(films[1])))
    planets.add(Planet(5, "Dagobah", "murky", "N/A", 10000, listOf(films[1], films[5])))
    planets.add(Planet(6, "Bespin", "temperate", "1.5 (surface), 1 standard (Cloud City)", 6000000, listOf(films[1])))
    planets.add(Planet(7, "Endor", "forests, mountains, lakes", "0.85 standard", 30000000, listOf(films[2])))
    planets.add(Planet(8, "Naboo", "temperate", "1 standard", 4500000000, listOf(films[3], films[4], films[5])))

    vehicles.add(Vehicle(1, "Snowspeeder", "t-47 airspeeder", "Incom corporation", 4, 2, 0, 10, "airspeeder", listOf(films[1])))
    vehicles.add(Vehicle(2, "Imperial Speeder Bike", "74-Z speeder bike", "Aratech Repulsor Company", 3, 1, 1, 4, "speeder", listOf(films[2])))

    starships.add(Starship(1, "X-wing", "T-65 X-wing", "Incom Corporation", 12, 1, 0, 110, "starfighter", listOf(films[0],films[1],films[2])))
    starships.add(Starship(2, "Imperial shuttle", "Lambda-class T-4a shuttle", "Sienar Fleet Systems", 20, 6, 20, 80000, "armed government transport", listOf(films[1],films[2])))
    starships.add(Starship(3, "TIE Advanced x1", "Twin Ion Engine Advanced x1", "Sienar Fleet Systems", 9, 1, 0, 150, "starfighter", listOf(films[1])))

    people.add(Person(1, "Luke Skywalker", "19BBY", planets[0], listOf(films[0], films[1], films[2], films[5]), listOf(vehicles[0], vehicles[1]), listOf(starships[0], starships[1])))
    people.add(Person(2, "C-3PO", "112BBY", planets[0], listOf(films[0], films[1], films[2], films[3], films[4], films[5]), listOf(), listOf()))
    people.add(Person(3, "R2-D2", "33BBY", planets[7], listOf(films[0], films[1], films[2], films[3], films[4], films[5]), listOf(), listOf()))
    people.add(Person(4, "Darth Vader", "41.9BBY", planets[0], listOf(films[0], films[1], films[2], films[5]), listOf(), listOf(starships[2])))
  }

  fun getFilms(limit: Int?): List<Film> {
    return films.subList(0, limit ?: films.size)
  }
  fun getFilm(id: Int): Film? = films.find { it.id == id }
  fun getPlanets(limit: Int?): List<Planet> {
    return planets.subList(0, limit ?: planets.size)
  }
  fun getPlanet(id: Int): Planet? = planets.find { it.id == id }
  fun getPeople(limit: Int?): List<Person> {
    return people.subList(0, limit ?: people.size)
  }
  fun getPerson(id: Int): Person? = people.find { it.id == id }
  fun getPersonByFilm(film: Film): List<Person> = people.filter { p -> p.films.any { f -> f.id == film.id } }
  fun getPersonByPlanet(planet: Planet): List<Person> = people.filter { p -> p.homeWorld.id == planet.id }
}
