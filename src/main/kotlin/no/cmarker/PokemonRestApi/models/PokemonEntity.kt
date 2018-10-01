package no.cmarker.PokemonRestApi.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * @author Christian Marker on 24/09/2018 at 11:02.
 */
@Entity(name = "pokemons")
class PokemonEntity (
		
		@get:NotBlank
		@get:Size(max = 124)
		var imgUrl: String,
		
		@get:NotNull
		@get:Max(999)
		var number: Int,
		
		@get:NotBlank
		@get:Size(max = 124)
		var name: String,
		
		@get:NotBlank
		var type: String,
		
		@get:Id
		@get:GeneratedValue(strategy = GenerationType.IDENTITY)
		var id: Long? = null
)