package no.cmarker.PokemonRestApi.dto

import no.cmarker.PokemonRestApi.utils.ResponseStatus
import no.cmarker.PokemonRestApi.utils.WrappedResponse

/**
 * @author Christian Marker on 28/09/2018 at 11:55.
 */
class ResponseDto (
		code: Int? = null,
		page: PageDto<PokemonDto>? = null,
		message: String? = null,
		status: ResponseStatus? = null
) : WrappedResponse<PokemonDto>(code, page, message, status)