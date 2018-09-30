package no.cmarker.PokemonRestApi.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Throwables
import io.swagger.annotations.*
import no.cmarker.PokemonRestApi.utils.DtoConverters
import no.cmarker.PokemonRestApi.dto.PokemonDto
import no.cmarker.PokemonRestApi.dto.ResponseDto
import no.cmarker.PokemonRestApi.models.hal.HalLink
import no.cmarker.PokemonRestApi.utils.WrappedResponse
import no.cmarker.PokemonRestApi.repository.PokemonRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import javax.validation.ConstraintViolationException

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
	private lateinit var crud: PokemonRepository
	
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
		
		
		if (offset < 0 || limit < 1) {
			return ResponseEntity.status(400).body(
					ResponseDto(
							code = 400,
							message = "Invalid offset or limit. Offset need to be a positive number, and limit need to be greater than 1."
					).validated()
			)
		}
		
		val pokemonResultList: List<PokemonDto>
		val builder = UriComponentsBuilder.fromPath("/pokemon")
		
		
		// If no params are defined, return all data in database
		if (paramId.isNullOrBlank() && paramType.isNullOrBlank()) {
			
			pokemonResultList = DtoConverters.transform(crud.findAll())
			
		}
		
		// If only paramId is defined, the pokemon with that id
		else if (!paramId.isNullOrBlank() && paramType.isNullOrBlank()) {
			
			val id = try {
				// Here i try to parse the id given as String in the URL to a long value
				paramId!!.toLong()
				
			} catch (e: Exception) {
				
				return ResponseEntity.status(404).body(
						ResponseDto(
								code = 404,
								message = "Invalid id: $paramId"
						).validated()
				)
				
			}
			
			// Getting entity from DB
			val entity = crud.findById(id).orElse(null)
					?: return ResponseEntity.status(404).body(
							ResponseDto(
									code = 404,
									message = "could not find pokemon with ID: $id"
							).validated()
					)
			
			pokemonResultList = listOf(DtoConverters.transform(entity))
			
			builder.queryParam("id", id)
			
		}
		
		// Else If only paramType is defined, return all pokemon in that type
		else {
			
			pokemonResultList = DtoConverters.transform(crud.findAllByType(paramType!!))
			
			builder.queryParam("type", paramType)
		}
		
		if (offset != 0 && offset >= pokemonResultList.size) {
			
			return ResponseEntity.status(400).body(
					ResponseDto(
							code = 400,
							message = "Too large offset, size of result is ${pokemonResultList.size}"
					).validated()
			)
			
		}
		
		builder.queryParam("limit", limit)
		
		val dto = DtoConverters.transform(pokemonResultList, offset, limit)
		
		dto._self = HalLink(builder.cloneBuilder()
				.queryParam("offset", offset)
				.build().toString()
		)
		
		if (!pokemonResultList.isEmpty() && offset > 0) {
			
			dto.prev = HalLink(builder.cloneBuilder()
					.queryParam("offset", Math.max(offset - limit, 0))
					.build().toString()
			)
		}
		
		if (offset + limit < pokemonResultList.size) {
			
			dto.next = HalLink(builder.cloneBuilder()
					.queryParam("offset", offset + limit)
					.build().toString()
			)
		}
		
		return ResponseEntity.status(200).body(
				ResponseDto(
						code = 200,
						page = dto
						
				).validated()
		)
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
		
		/*
			Error code 400: User error
		*/
		
		if (dto.id != null) {
			return ResponseEntity.status(400).body(
					ResponseDto(
							code = 400,
							message = "id != null, you cannot create a pokemon with predefined id"
					).validated()
			)
		}
		val id: Long?
		
		if (dto.name == null || dto.number == null || dto.type == null || dto.imgUrl == null) {
			return ResponseEntity.status(400).body(
					ResponseDto(
							code = 400,
							message = "you need to spesify a name, number, type and imgUrl when creating a Pokemon"
					).validated()
			)
		}
		
		try {
			id = crud.createPokemon(dto.number!!, dto.name!!, dto.type!!, dto.imgUrl!!)
			
		} catch (e: Exception) {
			
			if (Throwables.getRootCause(e) is ConstraintViolationException) {
				return ResponseEntity.status(400).body(
						ResponseDto(
								code = 400,
								message = "Error while creating a pokemon, contact sys-adm"
						).validated()
				)
			}
			throw e
		}
		return ResponseEntity.status(201).body(
				ResponseDto(
						code = 201,
						message = "Pokemon with id: $id successfully created"
				).validated()
		)
		
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
			return ResponseEntity.status(400).body(
					ResponseDto(
							code = 400,
							message = "Invalid id: $paramId"
					).validated()
			)
			
		}
		
		//if the given is is not registred in the DB
		if (!crud.existsById(id)) {
			return ResponseEntity.status(404).body(
					ResponseDto(
							code = 404,
							message = "Could not find pokemon with id: $id"
					).validated()
			)
		}
		
		crud.deleteById(id)
		return ResponseEntity.status(204).body(
				ResponseDto(
						code = 204,
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
			return ResponseEntity.status(404).body(
					ResponseDto(
							code = 404,
							message = "Invalid id: $paramId"
					).validated()
			)
		}
		
		if (updatedPokemonDto.id != paramId) {
			
			// return 409 (Conflict)
			return ResponseEntity.status(409).body(
					ResponseDto(
							code = 409,
							message = "The id given in the URL doesn't match the one in the JSON-obj sent as body"
					).validated()
			)
		}
		
		if (!crud.existsById(id)) {
			
			// return 404 (Not found)
			return ResponseEntity.status(404).body(
					ResponseDto(
							code = 404,
							message = "Could not find pokemon with id: $id"
					).validated()
			)
		}
		
		try {
			
			println("ALL OK! READY TO CHANGE THE ENTITY")
			crud.updatePokemon(id, updatedPokemonDto.name!!, updatedPokemonDto.type!!, updatedPokemonDto.number!!, updatedPokemonDto.imgUrl!!)
			
		} catch (e: ConstraintViolationException) {
			return ResponseEntity.status(400).body(
					ResponseDto(
							code = 404,
							message = "Error while updating pokemon"
					).validated()
			)
		}
		
		return ResponseEntity.status(204).body(
				ResponseDto(
						code = 204,
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
			
			return ResponseEntity.status(404).body(
					ResponseDto(
							code = 404,
							message = "Invalid id: $paramId"
					).validated()
			)
		}
		
		if (!crud.existsById(id)) {
			
			// return 404 (Not found)
			return ResponseEntity.status(404).body(
					ResponseDto(
							code = 404,
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
			return ResponseEntity.status(409).body(
					ResponseDto(
							code = 404,
							message = "Invalid JSON data"
					).validated()
			)
		}
		
		val newNumber: Int
		
		
		// Updating the id is not allowed
		if (jsonNode.has("id")) {
			return ResponseEntity.status(400).body(
					ResponseDto(
							code = 400,
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
				return ResponseEntity.status(400).body(
						ResponseDto(
								code = 400,
								message = "Number has to be numeral"
						).validated()
				)
			}
			
			crud.updateNumber(id, newNumber)
			
		}
		return ResponseEntity.status(204).body(
				ResponseDto(
						code = 204,
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
	@Deprecated(message = "Use the new method", level = DeprecationLevel.HIDDEN)
	fun deprecatedGetAllPokemonByType(@ApiParam("Type of Pokemon")
									  @PathVariable("type")
									  paramType: String?): ResponseEntity<List<PokemonDto>> {
		
		println("ENTERED DEPRECATED GetPokemonByType METHOD")
		
		return ResponseEntity
				.status(301)
				.location(UriComponentsBuilder.fromUriString("$uriPath/?type=$paramType").build().toUri())
				.build()
		
	}
	
	
	@ApiOperation("Get a pokemon based on id")
	@ApiResponses(ApiResponse(code = 301, message = "Deprecated URI. Moved permanently."))
	@GetMapping("/id/{id}")
	@Deprecated(message = "Use the new method", level = DeprecationLevel.HIDDEN)
	fun deprecatedGetPokemonById(@ApiParam("Id of the pokemon")
								 @PathVariable("id")
								 paramId: String?): ResponseEntity<PokemonDto> {
		
		println("ENTERED DEPRECATED GetPokemonById METHOD")
		
		return ResponseEntity
				.status(301)
				.location(UriComponentsBuilder.fromUriString("$uriPath/?id=$paramId").build().toUri())
				.build()
	}
	
	
	@ApiOperation("Delete a pokemon with the given id")
	@ApiResponses(ApiResponse(code = 302, message = "Deprecated URI. Moved permanently."))
	@DeleteMapping(path = ["/id/{id}"])
	@Deprecated(message = "Use the new method", level = DeprecationLevel.HIDDEN)
	fun deprecatedDeletePokemon(@ApiParam("id of pokemon")
								@PathVariable("id")
								paramId: String?): ResponseEntity<Any> {
		
		println("ENTERED DEPRECATED DELETE METHOD")
		
		return ResponseEntity
				.status(302) // always use 308 to ALL other redirects than GET
				.location(UriComponentsBuilder.fromUriString("$uriPath?id=$paramId").build().toUri())
				.build()
		
	}
	
}