package no.cmarker.PokemonRestApi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PokemonRestApiApplication

fun main(args: Array<String>) {
    runApplication<PokemonRestApiApplication>(*args)
}
