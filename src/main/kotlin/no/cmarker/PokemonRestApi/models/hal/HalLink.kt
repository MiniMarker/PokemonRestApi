package no.cmarker.PokemonRestApi.models.hal

import io.swagger.annotations.ApiModelProperty

/**
 * @author Christian Marker on 28/09/2018 at 18:24.
 */
open class HalLink (
	
	@ApiModelProperty("URL of the link")
	var href: String = ""
)