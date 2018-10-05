package no.cmarker.PokemonRestApi.models.dto

import io.swagger.annotations.ApiModelProperty

/**
 * @author Christian Marker on 24/09/2018 at 11:01.
 */
data class PokemonDto(
		
		@ApiModelProperty("The id of the pokemon")
		var id: Long? = null,
		
		@ApiModelProperty("The number of the pokemon")
		var number: Int? = null,
		
		@ApiModelProperty("The name of the pokemon")
		var name: String? = null,
		
		@ApiModelProperty("The type of pokemon")
		var type: String? = null,
		
		@ApiModelProperty("The imgUrl of the pokemon")
		var imgUrl: String? = null

)