package no.cmarker.PokemonRestApi.models

import io.swagger.annotations.ApiModelProperty
import no.cmarker.PokemonRestApi.models.dto.PageDto
import no.cmarker.PokemonRestApi.models.dto.PokemonDto
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

/**
 * @author Christian Marker on 28/09/2018 at 11:31.
 */
open class WrappedResponse<T>(
		
		@ApiModelProperty("The returned HTTP status of the request")
		var code: Int? = null,
		
		@ApiModelProperty("The wrapped data-payload")
		var page: PageDto<PokemonDto>? = null,
		
		@ApiModelProperty("Error message in case of there was an error in the request")
		var message: String? = null,
		
		@ApiModelProperty("String representing either 'SUCCESS', 'ERROR' or 'FAIL', this is a simplified type of error-message and request-code")
		var status: ResponseStatus? = null

) {
	
	fun validated(): WrappedResponse<T> {
		
		//checks if the request-code is NULL
		val c: Int = (code ?: throw IllegalStateException("Missing HTTP code"))
		
		//checkin that the request-code is legal
		if (c !in 100..599) {
			throw IllegalStateException("invalid HTTP-code: $code")
		}
		
		// setting status message to correct enum-value
		// if it is already set, i need to validate that the message is correct
		if (status == null) {
			status = when (c) {
				in 100..399 -> ResponseStatus.SUCCESS        // All OK
				in 400..499 -> ResponseStatus.ERROR          // User error
				in 500..599 -> ResponseStatus.FAIL           // Server error
				else -> throw IllegalStateException("invalid HTTP-code: $code")
			}
			
		} else {
			
			val wrongSuccess = (status == ResponseStatus.SUCCESS && c !in 100..399)
			val wrongError = (status == ResponseStatus.ERROR && c !in 400..499)
			val wrongFail = (status == ResponseStatus.FAIL && c !in 500..599)
			
			if (wrongSuccess || wrongError || wrongFail) {
				throw IllegalArgumentException("Status $status is not correct for the given HTTP code $c")
			}
		}
		
		//validating that all errors MUST have a message
		if (status != ResponseStatus.SUCCESS && message == null) {
			throw IllegalArgumentException("Failed response, but with no error message in result")
		}
		
		//returning the Wrapped response
		return this
	}
}