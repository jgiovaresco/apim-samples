package io.apim.samples.core.starwars

import java.time.LocalDate

class Film(val id: Int, val title: String, val episode: Int, val director: String, val releaseDate: LocalDate)

class Planet(val id: Int, val name: String, val climate: String, val gravity: String, val population: Long, val films: List<Film>)

class Vehicle(val id: Int, val name: String, val model: String, val manufacturer: String, val length: Int, val crew: Int, val passengers: Int, val cargoCapacity: Int, val vehicleClass: String, val films: List<Film>)

class Starship(val id: Int, val name: String, val model: String, val manufacturer: String, val length: Int, val crew: Int, val passengers: Int, val cargoCapacity: Int, val starshipClass: String, val films: List<Film>)

class Person(val id: Int, val name: String, val birthYear: String, val homeWorld: Planet, val films: List<Film>, val vehicles: List<Vehicle>, val starships: List<Starship>)
