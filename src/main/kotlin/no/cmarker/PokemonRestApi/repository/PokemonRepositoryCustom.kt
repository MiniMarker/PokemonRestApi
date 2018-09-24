package no.cmarker.PokemonRestApi.repository

import org.springframework.transaction.annotation.Transactional

/**
 * @author Christian Marker on 24/09/2018 at 11:08.
 */

@Transactional
interface PokemonRepositoryCustom {
	
	fun createPokemon(number: Int, name: String, type: String): Long
	
	fun updatePokemon(id: Long, name: String, type: String, number: Int): Boolean
	
	fun updateName(id: Long, name: String): Boolean
	
	fun updateNumber(id: Long, newNumber: Int): Boolean
	
}