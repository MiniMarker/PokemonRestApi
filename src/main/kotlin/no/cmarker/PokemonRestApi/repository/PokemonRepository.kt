package no.cmarker.PokemonRestApi.repository

import no.cmarker.PokemonRestApi.models.PokemonEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

/**
 * @author Christian Marker on 24/09/2018 at 11:07.
 */
@Repository
interface PokemonRepository : CrudRepository<PokemonEntity, Long>, PokemonRepositoryCustom {
	
	fun findAllByType(type: String): Iterable<PokemonEntity>
}