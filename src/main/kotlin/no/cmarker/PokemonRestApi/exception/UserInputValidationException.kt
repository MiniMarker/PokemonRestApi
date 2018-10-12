package no.cmarker.PokemonRestApi.exception

/**
 * Created by arcuri82
 */
class UserInputValidationException(
        message: String,
        val httpCode : Int = 400
) : RuntimeException(message)