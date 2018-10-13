package no.cmarker.PokemonRestApi.service

import no.cmarker.PokemonRestApi.models.WrappedResponse
import no.cmarker.PokemonRestApi.models.dto.PokemonDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

/**
 * @author Christian Marker on 13/10/2018 at 19:47.
 */

@Service
class PokemonServiceDeprecated {
	
	// ###############################################
	// DEPRECATED FUNCTIONS
	// REDIRECTS TO NEW VERSIONS
	//
	//	Terminal curl command
	//	curl -X "DELETE" -L <URL>
	//
	//		Flags:
	//			-v		Verbose (Get logs)
	//			-X 		Needed if you are doing other actions than GET
	//			-L 		Follow redirects
	//
	// ###############################################
	
	val uriPath: String = "/pokemon"
	
	fun deprecatedGetAllPokemonByType(paramType: String?) : ResponseEntity<WrappedResponse<PokemonDto>> {
		
		println("ENTERED DEPRECATED GetPokemonByType METHOD")
		
		return ResponseEntity
			.status(HttpStatus.MOVED_PERMANENTLY)
			.location(
				UriComponentsBuilder
					.fromUriString("$uriPath/?type=$paramType")
					.build()
					.toUri()
			).build()
	}
	
	fun deprecatedGetPokemonById(paramId: String?) : ResponseEntity<WrappedResponse<PokemonDto>> {
		
		println("ENTERED DEPRECATED GetPokemonById METHOD")
		
		return ResponseEntity
			.status(HttpStatus.MOVED_PERMANENTLY)
			.location(
				UriComponentsBuilder
					.fromUriString("$uriPath/?id=$paramId")
					.build()
					.toUri()
			).build()
	}
	
	fun deprecatedDeletePokemon(paramId: String?) : ResponseEntity<WrappedResponse<PokemonDto>> {
		
		println("ENTERED DEPRECATED DELETE METHOD")
		
		return ResponseEntity
			.status(HttpStatus.PERMANENT_REDIRECT) // always use 308 to ALL other redirects than GET
			.location(
				UriComponentsBuilder
					.fromUriString("$uriPath?id=$paramId")
					.build()
					.toUri()
			).build()
		
	}
	
}