package no.cmarker.PokemonRestApi.models.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import no.cmarker.PokemonRestApi.models.hal.HalLink
import no.cmarker.PokemonRestApi.models.hal.HalObject

/**
 * @author Christian Marker on 28/09/2018 at 18:03.
 */

@ApiModel(description = "Paginated list of data with HAL links at pagination technique")
class PageDto<T>(
		
		@get:ApiModelProperty("List of pokemon in the current recieved page")
		var data: List<T> = listOf(),
		
		@get:ApiModelProperty("The index of the first element on the page")
		var rangeMin: Int = 0,
		
		@get:ApiModelProperty("The index of the last element on the page")
		var rangeMax: Int = 0,
		
		@get:ApiModelProperty("Number of all elements in result")
		var totalSize: Int = 0,
		
		next: HalLink? = null,
		prev: HalLink? = null,
		_self: HalLink? = null
		
) : HalObject() {
	
	
	@get:JsonIgnore
	var next: HalLink?
		set(value) {
			if (value != null) {
				_links["next"] = value
			} else {
				_links.remove("next")
			}
		}
		
		get() = _links["next"]
	
	@get:JsonIgnore
	var prev: HalLink?
		set(value) {
			if (value != null) {
				_links["prev"] = value
			} else {
				_links.remove("prev")
			}
		}
		
		get() = _links["prev"]
	
	@get:JsonIgnore
	var _self: HalLink?
		set(value) {
			if (value != null) {
				_links["self"] = value
			} else {
				_links.remove("self")
			}
		}
		
		get() = _links["self"]
	
	
	init {
		this.next = next
		this.prev = prev
		this._self = _self
	}
}