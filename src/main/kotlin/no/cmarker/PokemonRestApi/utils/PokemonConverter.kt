package no.cmarker.PokemonRestApi.utils

import no.cmarker.PokemonRestApi.dto.PokemonDto
import no.cmarker.PokemonRestApi.models.PokemonEntity

/**
 * @author Christian Marker on 24/09/2018 at 11:00.
 */
class PokemonConverter {
	
	companion object {
		
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
	
	
}