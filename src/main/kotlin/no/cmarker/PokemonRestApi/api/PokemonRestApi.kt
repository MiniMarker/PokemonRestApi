package no.cmarker.PokemonRestApi.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Throwables
import io.swagger.annotations.*
import no.cmarker.PokemonRestApi.models.dto.PokemonDto
import no.cmarker.PokemonRestApi.models.dto.ResponseDto
import no.cmarker.PokemonRestApi.models.WrappedResponse
import no.cmarker.PokemonRestApi.models.dto.PageDto
import no.cmarker.PokemonRestApi.repository.PokemonRepository
import no.cmarker.PokemonRestApi.service.PokemonService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import javax.validation.ConstraintViolationException
import org.springframework.http.HttpStatus.*

/**
 * @author Christian Marker on 24/09/2018 at 11:00.
 */

//TODO Make a separate table with evolve info for all pokemons
//TODO Move all code to the Repositories


@Api(value = "/pokemon", description = "Handling of creating and retrieving pokemons")
@RequestMapping(
		path = ["/pokemon"],
		produces = [(MediaType.APPLICATION_JSON_VALUE)]
)
@RestController
class PokemonRestApi {
	
	@Autowired
	private lateinit var repository: PokemonRepository
	
	@Autowired
	private lateinit var service: PokemonService;
	
	/*
		GET
	 */
	
	@ApiOperation("Get pokemon")
	@GetMapping
	fun get(
			@ApiParam("Id if the pokemon")
			@RequestParam("id", required = false)
			paramId: String?,
			//
			@ApiParam("Type of the pokemons")
			@RequestParam("type", required = false)
			paramType: String?,
			//
			@ApiParam("Offset in the list of news")
			@RequestParam("offset", defaultValue = "0")
			offset: Int,
			//
			@ApiParam("Limit of news in a single retrieved page")
			@RequestParam("limit", defaultValue = "10")
			limit: Int
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
					  @RequestBody
					  dto: PokemonDto): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		return service.createPokemon(dto)
		
	}
	
	/*
		DELETE
	 */
	
	@ApiOperation("Delete a pokemon with the given id")
	@DeleteMapping
	fun deletePokemon(@ApiParam("id of pokemon")
					  @RequestParam("id", required = true)
					  paramId: String?): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		println("DELETEING")
		
		val id: Long?
		
		try {
			id = paramId!!.toLong()
			
		} catch (e: Exception) {
			return ResponseEntity.status(BAD_REQUEST).body(
					ResponseDto(
							code = BAD_REQUEST.value(),
							message = "Invalid id: $paramId"
					).validated()
			)
			
		}
		
		//if the given is is not registred in the DB
		if (!repository.existsById(id)) {
			return ResponseEntity.status(NOT_FOUND).body(
					ResponseDto(
							code = NOT_FOUND.value(),
							message = "Could not find pokemon with id: $id"
					).validated()
			)
		}
		
		repository.deleteById(id)
		return ResponseEntity.status(NO_CONTENT).body(
				ResponseDto(
						code = NO_CONTENT.value(),
						message = "Pokemon with id: $id successfully deleted"
				).validated()
		)
		
	}
	
	/*
		PUT
	 */
	
	@ApiOperation("Update all info for a given pokemon")
	@PutMapping(path = ["/id/{id}"], consumes = [(MediaType.APPLICATION_JSON_VALUE)])
	fun updatePokemon(@ApiParam("id of pokemon")
					  @PathVariable("id")
					  paramId: Long?,
	
					  @ApiParam("The updated pokemon data")
					  @RequestBody
					  updatedPokemonDto: PokemonDto): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		val id: Long
		
		try {
			
			id = updatedPokemonDto.id!!.toLong()
			
		} catch (e: Exception) {
			
			// Invalid id, could not parse to long
			// return 404 (Not found)
			return ResponseEntity.status(NOT_FOUND).body(
					ResponseDto(
							code = NOT_FOUND.value(),
							message = "Invalid id: $paramId"
					).validated()
			)
		}
		
		if (updatedPokemonDto.id != paramId) {
			
			// return 409 (Conflict)
			return ResponseEntity.status(CONFLICT).body(
					ResponseDto(
							code = CONFLICT.value(),
							message = "The id given in the URL doesn't match the one in the JSON-obj sent as body"
					).validated()
			)
		}
		
		if (!repository.existsById(id)) {
			
			// return 404 (Not found)
			return ResponseEntity.status(NOT_FOUND).body(
					ResponseDto(
							code = NOT_FOUND.value(),
							message = "Could not find pokemon with id: $id"
					).validated()
			)
		}
		
		try {
			
			println("ALL OK! READY TO CHANGE THE ENTITY")
			repository.updatePokemon(id, updatedPokemonDto.name!!, updatedPokemonDto.type!!, updatedPokemonDto.number!!, updatedPokemonDto.imgUrl!!)
			
		} catch (e: ConstraintViolationException) {
			return ResponseEntity.status(BAD_REQUEST).body(
					ResponseDto(
							code = BAD_REQUEST.value(),
							message = "Error while updating pokemon"
					).validated()
			)
		}
		
		return ResponseEntity.status(NO_CONTENT).body(
				ResponseDto(
						code = NO_CONTENT.value(),
						message = "Pokemon with id: $id successfully updated"
				).validated()
		)
	}
	
	/*
		PATCH
	 */
	
	@ApiOperation("Modify then number for the given pokemon")
	@PatchMapping(
			path = ["/id/{id}"],
			consumes = ["application/merge-patch+json"])
	fun patchNumber(@ApiParam("The id of the pokemon to update")
					@PathVariable("id")
					paramId: Long?,
	
					@ApiParam("The partial patch (number only).")
					@RequestBody
					jsonPatch: String): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		val id: Long
		
		try {
			// Here i try to parse the id given as String in the URL to a long value
			id = paramId!!.toLong()
			
		} catch (e: Exception) {
			
			return ResponseEntity.status(NOT_FOUND).body(
					ResponseDto(
							code = NOT_FOUND.value(),
							message = "Invalid id: $paramId"
					).validated()
			)
		}
		
		if (!repository.existsById(id)) {
			
			// return 404 (Not found)
			return ResponseEntity.status(NOT_FOUND).body(
					ResponseDto(
							code = NOT_FOUND.value(),
							message = "Could not find pokemon with id: $id"
					).validated()
			)
		}
		
		val jackson = ObjectMapper()
		val jsonNode: JsonNode
		
		try {
			jsonNode = jackson.readValue(jsonPatch, JsonNode::class.java)
		} catch (e: Exception) {
			
			//Invalid JSON data
			return ResponseEntity.status(CONFLICT).body(
					ResponseDto(
							code = CONFLICT.value(),
							message = "Invalid JSON data"
					).validated()
			)
		}
		
		val newNumber: Int
		
		
		// Updating the id is not allowed
		if (jsonNode.has("id")) {
			return ResponseEntity.status(BAD_REQUEST).body(
					ResponseDto(
							code = BAD_REQUEST.value(),
							message = "Updating the id is not allowed"
					).validated()
			)
		}
		
		// Validating data in json body
		if (jsonNode.has("number")) {
			
			val numberNode = jsonNode.get("number")
			
			if (numberNode.isNumber && !numberNode.isNull) {
				
				//All checks ok!
				newNumber = numberNode.asInt()
				
			} else {
				return ResponseEntity.status(BAD_REQUEST).body(
						ResponseDto(
								code = BAD_REQUEST.value(),
								message = "Number has to be numeral"
						).validated()
				)
			}
			
			repository.updateNumber(id, newNumber)
			
		}
		return ResponseEntity.status(NO_CONTENT).body(
				ResponseDto(
						code = NO_CONTENT.value(),
						message = "Pokemon with id: $id successfully patched"
				).validated()
		)
		
	}
	
	
	// DEPRECATED FUNCTIONS ############################################
	
	val uriPath: String = "/pokemon"
	
	/**
	 * This function uses the findAllByType() that CrudRepository<T, K> automatically creates when i specifies
	 * findAllBy{param} in the interface that extends CrudRepository<T, K>
	 */
	@ApiOperation("Get all pokemons by given type")
	@ApiResponses(ApiResponse(code = 301, message = "Deprecated URI. Moved permanently."))
	@GetMapping("/type/{type}")
	@Deprecated(message = "Use the new method")
	fun deprecatedGetAllPokemonByType(@ApiParam("Type of Pokemon")
									  @PathVariable("type")
									  paramType: String?): ResponseEntity<List<PokemonDto>> {
		
		println("ENTERED DEPRECATED GetPokemonByType METHOD")
		
		return ResponseEntity
				.status(MOVED_PERMANENTLY)
				.location(
						UriComponentsBuilder
								.fromUriString("$uriPath/?type=$paramType")
								.build()
								.toUri()
				).build()
		
	}
	
	
	@ApiOperation("Get a pokemon based on id")
	@ApiResponses(ApiResponse(code = 301, message = "Deprecated URI. Moved permanently."))
	@GetMapping("/id/{id}")
	@Deprecated(message = "Use the new method")
	fun deprecatedGetPokemonById(@ApiParam("Id of the pokemon")
								 @PathVariable("id")
								 paramId: String?): ResponseEntity<PokemonDto> {
		
		println("ENTERED DEPRECATED GetPokemonById METHOD")
		
		return ResponseEntity
				.status(MOVED_PERMANENTLY)
				.location(
						UriComponentsBuilder
								.fromUriString("$uriPath/?id=$paramId")
								.build()
								.toUri()
				).build()
	}
	
	
	@ApiOperation("Delete a pokemon with the given id")
	@ApiResponses(ApiResponse(code = 302, message = "Deprecated URI. Moved permanently."))
	@DeleteMapping(path = ["/id/{id}"])
	@Deprecated(message = "Use the new method")
	fun deprecatedDeletePokemon(@ApiParam("id of pokemon")
								@PathVariable("id")
								paramId: String?): ResponseEntity<Any> {
		
		println("ENTERED DEPRECATED DELETE METHOD")
		
		return ResponseEntity
				.status(PERMANENT_REDIRECT) // always use 308 to ALL other redirects than GET
				.location(
						UriComponentsBuilder
								.fromUriString("$uriPath?id=$paramId")
								.build()
								.toUri()
				).build()
		
	}
	
}