package no.cmarker.PokemonRestApi.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Throwables
import io.swagger.annotations.*
import no.cmarker.PokemonRestApi.dto.PokemonConverter
import no.cmarker.PokemonRestApi.dto.PokemonDto
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
@Api(value = "/pokemon", description = "Handling of creating and retrieving pokemons")
@RequestMapping(
		path = ["/pokemon"],
		produces = [(MediaType.APPLICATION_JSON_VALUE)]
)
@RestController
class PokemonRestApi {
	
	//TODO Move all code to the Repositories
	
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
			paramType: String?
	): ResponseEntity<Any> {
		
		// If no params are defined, return all data in database
		if (paramId.isNullOrBlank() && paramType.isNullOrBlank()) {
			
			return ResponseEntity.ok(PokemonConverter.transform(crud.findAll()))
			
		}
		
		// If only paramId is defined, the pokemon with that id
		else if (!paramId.isNullOrBlank() && paramType.isNullOrBlank()) {
			
			val id: Long
			
			try {
				// Here i try to parse the id given as String in the URL to a long value
				id = paramId!!.toLong()
				
			} catch (e: Exception) {
				
				return ResponseEntity.status(404).build()
				
			}
			
			// Getting entity from DB
			val entity = crud.findById(id).orElse(null) ?: return ResponseEntity.status(404).build()
			
			return ResponseEntity.ok(PokemonConverter.transform(entity))
			
		}
		
		// Else If only paramType is defined, return all pokemon in that type
		else {
			return ResponseEntity.ok(PokemonConverter.transform(crud.findAllByType(paramType!!)))
			
		}
		
	}
	
	/*
		POST
	 */
	
	@ApiOperation("Create a pokemon")
	@PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
	@ApiResponse(code = 201, message = "The id of newly created pokemon")
	fun createPokemon(@ApiParam("Pokemon number, name and type")
					  @RequestBody
					  dto: PokemonDto): ResponseEntity<Long> {
		
		/*
			Error code 400: User error
		*/
		
		if (dto.id != null) {
			return ResponseEntity.status(400).build()
		}
		val id: Long?
		
		if (dto.name == null || dto.number == null || dto.type == null || dto.imgUrl == null) {
			return ResponseEntity.status(400).build()
		}
		
		try {
			id = crud.createPokemon(dto.number!!, dto.name!!, dto.type!!, dto.imgUrl!!)
			
		} catch (e: Exception) {
			
			if (Throwables.getRootCause(e) is ConstraintViolationException) {
				return ResponseEntity.status(400).build()
			}
			throw e
		}
		return ResponseEntity.status(201).body(id)
		
	}
	
	/*
		DELETE
	 */
	
	@ApiOperation("Delete a pokemon with the given id")
	@DeleteMapping
	fun deletePokemon(@ApiParam("id of pokemon")
					  @RequestParam("id", required = true)
					  paramId: String?): ResponseEntity<Any> {
		
		println("DELETEING")
		
		val id: Long?
		
		try {
			id = paramId!!.toLong()
			
		} catch (e: Exception) {
			return ResponseEntity.status(400).build()
			
		}
		
		//if the given is is not registred in the DB
		if (!crud.existsById(id)) {
			return ResponseEntity.status(404).build()
		}
		
		crud.deleteById(id)
		return ResponseEntity.status(204).build()
		
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
					  updatedPokemonDto: PokemonDto): ResponseEntity<Any> {
		
		val id: Long
		
		try {
			
			id = updatedPokemonDto.id!!.toLong()
			
		} catch (e: Exception) {
			
			// Invalid id, could not parse to long
			// return 404 (Not found)
			return ResponseEntity.status(404).build()
		}
		
		if (updatedPokemonDto.id != paramId) {
			
			// The id given in the URL doesn't match the one in the JSON-obj sent as body
			// return 409 (Conflict)
			return ResponseEntity.status(409).build()
		}
		
		if (!crud.existsById(id)) {
			
			// if the pokemon doesn't exist in the datebase
			// return 404 (Not found)
			return ResponseEntity.status(404).build()
		}
		
		try {
			
			println("ALL OK! READY TO CHANGE THE ENTITY")
			crud.updatePokemon(id, updatedPokemonDto.name!!, updatedPokemonDto.type!!, updatedPokemonDto.number!!, updatedPokemonDto.imgUrl!!)
			
		} catch (e: ConstraintViolationException) {
			return ResponseEntity.status(400).build()
		}
		
		return ResponseEntity.status(204).build()
	}
	
	/*
		PATCH
	 */
	
	@ApiOperation("Modify then number for the given pokemon")
	@PatchMapping(
			path = ["/id/{id}"],
			consumes = ["application/merge-patch+json"])
	fun patchNumber(@ApiParam("The unique id of the pokemon to update")
					@PathVariable("id")
					paramId: Long?,
	
					@ApiParam("The partial patch (number only)")
					@RequestBody
					jsonPatch: String): ResponseEntity<Void> {
		
		val id: Long
		
		try {
			// Here i try to parse the id given as String in the URL to a long value
			id = paramId!!.toLong()
			
		} catch (e: Exception) {
			return ResponseEntity.status(404).build()
		}
		
		crud.findById(id) ?: return ResponseEntity.status(404).build()
		
		val jackson = ObjectMapper()
		val jsonNode: JsonNode
		
		try {
			jsonNode = jackson.readValue(jsonPatch, JsonNode::class.java)
		} catch (e: Exception) {
			
			//Invalid JSON data
			return ResponseEntity.status(409).build()
		}
		
		val newNumber: Int
		
		
		// Updating the id is not allowed
		if (jsonNode.has("id")) {
			return ResponseEntity.status(400).build()
		}
		
		// Validating data in json body
		if (jsonNode.has("number")) {
			
			val numberNode = jsonNode.get("number")
			
			if (numberNode.isNumber && !numberNode.isNull) {
				
				//All checks ok!
				newNumber = numberNode.asInt()
				
			} else {
				return ResponseEntity.status(400).build()
			}
			
			crud.updateNumber(id, newNumber)
			
		}
		return ResponseEntity.status(204).build()
		
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