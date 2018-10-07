package no.cmarker.PokemonRestApi.repository

import no.cmarker.PokemonRestApi.models.entity.PokemonEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

/**
 * @author Christian Marker on 24/09/2018 at 11:09.
 */

@Repository
@Transactional
class PokemonRepositoryImpl : PokemonRepositoryCustom {
	
	@Autowired
	private lateinit var em: EntityManager
	
	
	override fun createPokemon(number: Int, name: String, type: String, imgUrl: String): Long {
		
		val entity = PokemonEntity(imgUrl, number, name, type)
		em.persist(entity)
		return entity.id!!
		
	}
	
	override fun updateNumber(id: Long, newNumber: Int): Boolean {
		
		val entity = em.find(PokemonEntity::class.java, id) ?: return false
		
		entity.number = newNumber
		
		return true
	}
	
	override fun updateName(id: Long, name: String): Boolean {
		
		val entity = em.find(PokemonEntity::class.java, id) ?: return false
		
		entity.name = name
		
		return true
	}
	
	override fun updatePokemon(id: Long, name: String, type: String, number: Int, imgUrl: String) : Boolean {
		
		val pokemon = em.find(PokemonEntity::class.java, id) ?: return false
		
		pokemon.name = name
		pokemon.type = type
		pokemon.number = number
		pokemon.imgUrl = imgUrl
		
		return true
	}
}