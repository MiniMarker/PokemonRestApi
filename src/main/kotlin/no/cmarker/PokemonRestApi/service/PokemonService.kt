package no.cmarker.PokemonRestApi.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.base.Throwables
import no.cmarker.PokemonRestApi.models.WrappedResponse
import no.cmarker.PokemonRestApi.models.dto.PageDto
import no.cmarker.PokemonRestApi.models.dto.PokemonDto
import no.cmarker.PokemonRestApi.models.dto.ResponseDto
import no.cmarker.PokemonRestApi.models.hal.HalLink
import no.cmarker.PokemonRestApi.repository.PokemonRepository
import no.cmarker.PokemonRestApi.utils.DtoConverters
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import javax.validation.ConstraintViolationException

/**
 * @author Christian Marker on 05/10/2018 at 12:32.
 */

@Service
class PokemonService {
	
	@Autowired
	private lateinit var repository: PokemonRepository
	
	fun get(paramId: String?, paramType: String?, offset: Int, limit: Int) : ResponseEntity<WrappedResponse<PokemonDto>> {
		
		
		if (offset < 0 || limit < 1) {
			return ResponseEntity.status(BAD_REQUEST).body(
					ResponseDto(
							code = BAD_REQUEST.value(),
							message = "Invalid offset or limit. Offset need to be a positive number, and limit need to be greater than 1."
					).validated()
			)
		}
		
		val pokemonResultList: List<PokemonDto>
		val builder = UriComponentsBuilder.fromPath("/pokemon")
		
		// If no params are defined, return all data in database
		if (paramId.isNullOrBlank() && paramType.isNullOrBlank()) {
			
			pokemonResultList = DtoConverters.transform(repository.findAll())
			
		}
		
		// If only paramId is defined, the pokemon with that id
		else if (!paramId.isNullOrBlank() && paramType.isNullOrBlank()) {
			
			val id = try {
				// Here i try to parse the id given as String in the URL to a long value
				paramId!!.toLong()
				
			} catch (e: Exception) {
				
				return ResponseEntity.status(NOT_FOUND).body(
						ResponseDto(
								code = NOT_FOUND.value(),
								message = "Invalid id: $paramId"
						).validated()
				)
				
			}
			
			// Getting entity from DB
			val entity = repository.findById(id).orElse(null)
					?: return ResponseEntity.status(NOT_FOUND).body(
							ResponseDto(
									code = NOT_FOUND.value(),
									message = "could not find pokemon with ID: $id"
							).validated()
					)
			
			pokemonResultList = listOf(DtoConverters.transform(entity))
			
			builder.queryParam("id", id)
			
		}
		
		// Else If only paramType is defined, return all pokemon in that type
		else {
			
			pokemonResultList = DtoConverters.transform(repository.findAllByType(paramType!!))
			
			builder.queryParam("type", paramType)
		}
		
		
		if (offset != 0 && offset >= pokemonResultList.size) {
			
			return ResponseEntity.status(BAD_REQUEST).body(
					ResponseDto(
							code = BAD_REQUEST.value(),
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
		
		return ResponseEntity.status(OK).body(
				ResponseDto(
						code = OK.value(),
						page = dto
				
				).validated()
		)
	}
	
	fun createPokemon(dto: PokemonDto): ResponseEntity<WrappedResponse<PokemonDto>> {
		
		/*
			Error code 400: User error
		*/
		
		if (dto.id != null) {
			return ResponseEntity.status(NOT_FOUND).body(
					ResponseDto(
							code = NOT_FOUND.value(),
							message = "id != null, you cannot create a pokemon with predefined id"
					).validated()
			)
		}
		val id: Long?
		
		if (dto.name == null || dto.number == null || dto.type == null || dto.imgUrl == null) {
			return ResponseEntity.status(BAD_REQUEST).body(
					ResponseDto(
							code = BAD_REQUEST.value(),
							message = "you need to spesify a name, number, type and imgUrl when creating a Pokemon"
					).validated()
			)
		}
		
		try {
			id = repository.createPokemon(dto.number!!, dto.name!!, dto.type!!, dto.imgUrl!!)
			
		} catch (e: Exception) {
			
			if (Throwables.getRootCause(e) is ConstraintViolationException) {
				return ResponseEntity.status(BAD_REQUEST).body(
						ResponseDto(
								code = BAD_REQUEST.value(),
								message = "Error while creating a pokemon, contact sys-adm"
						).validated()
				)
			}
			throw e
		}
		
		return ResponseEntity.status(CREATED).body(
				ResponseDto(
						code = CREATED.value(),
						page = PageDto(data = listOf(PokemonDto(id = id))),
						message = "Pokemon with id: $id created"
				).validated()
		)
		
	}
	
	fun deletePokemon(paramId: String) : ResponseEntity<WrappedResponse<PokemonDto>> {
		
		println("DELETEING")
		
		val id: Long?
		
		try {
			id = paramId.toLong()
			
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
	
	fun updatePokemon(paramId: String?, updatedPokemonDto: PokemonDto) : ResponseEntity<WrappedResponse<PokemonDto>> {
		
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
		
		if (updatedPokemonDto.id != id) {
			
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
	
	fun patchNumber(paramId: String?, jsonPatch: String) : ResponseEntity<WrappedResponse<PokemonDto>> {
		
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
				.status(MOVED_PERMANENTLY)
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
				.status(MOVED_PERMANENTLY)
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
				.status(PERMANENT_REDIRECT) // always use 308 to ALL other redirects than GET
				.location(
						UriComponentsBuilder
								.fromUriString("$uriPath?id=$paramId")
								.build()
								.toUri()
				).build()
		
	}
	
}