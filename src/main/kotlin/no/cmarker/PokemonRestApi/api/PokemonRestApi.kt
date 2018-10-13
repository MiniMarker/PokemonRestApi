package no.cmarker.PokemonRestApi.api

import io.swagger.annotations.*
import no.cmarker.PokemonRestApi.models.dto.PokemonDto
import no.cmarker.PokemonRestApi.models.WrappedResponse
import no.cmarker.PokemonRestApi.service.PokemonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate

/**
 * @author Christian Marker on 24/09/2018 at 11:00.
 */

const val BASE_JSON = "application/json;charset=UTF-8"

//TODO Make a separate table with evolve info for all pokemons


@Api(value = "/pokemon", description = "Handling of creating and retrieving pokemons")
@RequestMapping(
		path = ["/pokemon"],
		produces = [(MediaType.APPLICATION_JSON_VALUE)]
)
@RestController
class PokemonRestApi {
	
	@Autowired
	private lateinit var service: PokemonService
	
	/*
		GET
	 */
	@CrossOrigin
	@ApiOperation("Get pokemon")
	@GetMapping(produces = [(MediaType.APPLICATION_JSON_VALUE)])
	fun get(
			@ApiParam("Id if the pokemon")
			@RequestParam("id", required = false) paramId: String?,
			//
			@ApiParam("Type of the pokemons")
			@RequestParam("type", required = false) paramType: String?,
			//
			@ApiParam("Offset in the list of news")
			@RequestParam("offset", defaultValue = "0") offset: Int,
			//
			@ApiParam("Limit of news in a single retrieved page")
			@RequestParam("limit", defaultValue = "10") limit: Int
	): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		return service.get(paramId, paramType, offset, limit)
		
	}
	
	/*
		POST
	 */
	
	@ApiOperation("Create a pokemon")
	@PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
	@ApiResponse(code = 201, message = "The id of newly created pokemon")
	fun createPokemon(@ApiParam("Pokemon number, name and type")
					  @RequestBody dto: PokemonDto
	): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		return service.createPokemon(dto)
		
	}
	
	/*
		DELETE
	 */
	
	@ApiOperation("Delete a pokemon with the given id")
	@DeleteMapping
	fun deletePokemon(@ApiParam("id of pokemon")
					  @RequestParam("id", required = true) paramId: String
	): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		return service.deletePokemon(paramId)
	}
	
	
	/*
		PUT
	 */
	
	@ApiOperation("Update all info for a given pokemon")
	@PutMapping(path = ["/id/{id}"], consumes = [(MediaType.APPLICATION_JSON_VALUE)])
	fun updatePokemon(@RequestHeader("If-Match") ifMatch: String?,
	
					  @ApiParam("id of pokemon")
					  @PathVariable("id") paramId: String?,
	
					  @ApiParam("The updated pokemon data")
					  @RequestBody updatedPokemonDto: PokemonDto
	): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		return service.updatePokemon(ifMatch, paramId, updatedPokemonDto)
		
	}
	
	/*
		PATCH
	 */
	
	@ApiOperation("Modify then number for the given pokemon")
	@PatchMapping(
			path = ["/id/{id}"],
			consumes = ["application/merge-patch+json"])
	fun patchNumber(@RequestHeader("If-Match") ifMatch: String?,
					
					@ApiParam("The id of the pokemon to update")
					@PathVariable("id") paramId: String?,
	
					@ApiParam("The partial patch (number only).")
					@RequestBody jsonPatch: String
	): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		return service.patchNumber(ifMatch, paramId, jsonPatch)
		
	}
	
	// DEPRECATED FUNCTIONS ############################################
	
	val uriPath: String = "/pokemon"
	
	/**
	 * This function uses the findAllByTypeIgnoreCase() that CrudRepository<T, K> automatically creates when i specifies
	 * findAllBy{param} in the interface that extends CrudRepository<T, K>
	 */
	@ApiOperation("Get all pokemons by given type")
	@ApiResponses(ApiResponse(code = 301, message = "Deprecated URI. Moved permanently."))
	@GetMapping("/type/{type}")
	@Deprecated(message = "Use the new method")
	fun deprecatedGetAllPokemonByType(@ApiParam("Type of Pokemon")
									  @PathVariable("type")
									  paramType: String?): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		return service.deprecatedGetAllPokemonByType(paramType)
		
	}
	
	
	@ApiOperation("Get a pokemon based on id")
	@ApiResponses(ApiResponse(code = 301, message = "Deprecated URI. Moved permanently."))
	@GetMapping("/id/{id}")
	@Deprecated(message = "Use the new method")
	fun deprecatedGetPokemonById(@ApiParam("Id of the pokemon")
								 @PathVariable("id")
								 paramId: String?): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		return service.deprecatedGetPokemonById(paramId)
	}
	
	
	@ApiOperation("Delete a pokemon with the given id")
	@ApiResponses(ApiResponse(code = 302, message = "Deprecated URI. Moved permanently."))
	@DeleteMapping(path = ["/id/{id}"])
	@Deprecated(message = "Use the new method")
	fun deprecatedDeletePokemon(@ApiParam("id of pokemon")
								@PathVariable("id")
								paramId: String?): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		return service.deprecatedDeletePokemon(paramId)
	}
}