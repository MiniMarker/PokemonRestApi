package no.cmarker.PokemonRestApi.models.hal

import io.swagger.annotations.ApiModelProperty

/**
 * @author Christian Marker on 28/09/2018 at 18:26.
 */
open class HalObject (
		
		@ApiModelProperty("HAL links")
		var _links: MutableMap<String, HalLink> = mutableMapOf()
)