package no.cmarker.PokemonRestApi.utils

import no.cmarker.PokemonRestApi.dto.PageDto
import no.cmarker.PokemonRestApi.dto.PokemonDto
import no.cmarker.PokemonRestApi.models.PokemonEntity
import kotlin.streams.toList

/**
 * @author Christian Marker on 24/09/2018 at 11:00.
 */

//object = static
object DtoConverters {
	
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
				totalSize = dtoList.size
		)
		
	}
	
	fun transform(entity: PokemonEntity): PokemonDto {
		
		return PokemonDto(
				id = entity.id,
				number = entity.number,
				name = entity.name,
				type = entity.type,
				imgUrl = entity.imgUrl
		)
		
	}
	
	// Transform multiple entities
	fun transform(entities: Iterable<PokemonEntity>): List<PokemonDto> {
		
		return entities.map { transform(it) }
		
	}
	
	
}