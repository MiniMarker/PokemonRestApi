package no.cmarker.PokemonRestApi.utils

import no.cmarker.PokemonRestApi.models.dto.PageDto
import no.cmarker.PokemonRestApi.models.dto.PokemonDto
import no.cmarker.PokemonRestApi.models.entity.PokemonEntity
import kotlin.streams.toList

/**
 * @author Christian Marker on 24/09/2018 at 11:00.
 */

//object = static
object DtoConverters {
	
	//List<PokemonDto> -> PageDto<PokemonDto>
	fun transform(pokemonList: List<PokemonDto>,
				  offset: Int,
				  limit: Int): PageDto<PokemonDto> {
		
		val dtoList: MutableList<PokemonDto> =
				pokemonList.stream()
						.skip(offset.toLong())
						.limit(limit.toLong())
						.toList().toMutableList()
		
		return PageDto(
				data = dtoList,
				rangeMin = offset,
				rangeMax = offset + dtoList.size - 1,
				totalSize = pokemonList.size
		)
		
	}
	
	//Single entitiy -> Dto
	fun transform(entity: PokemonEntity): PokemonDto {
		
		return PokemonDto(
				id = entity.id,
				number = entity.number,
				name = entity.name,
				type = entity.type,
				imgUrl = entity.imgUrl
		)
		
	}
	
	//Multiple entities -> List<PokemonDto>
	fun transform(entities: Iterable<PokemonEntity>): List<PokemonDto> {
		
		return entities.map { transform(it) }
		
	}
	
	
}