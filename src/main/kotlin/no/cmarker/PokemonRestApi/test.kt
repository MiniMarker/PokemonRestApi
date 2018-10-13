package no.cmarker.PokemonRestApi

import java.sql.Timestamp

/**
 * @author Christian Marker on 12/10/2018 at 13:04.
 */
object test {
	
	
	@JvmStatic
	fun main(args: Array<String>) {
		val timestamp = Timestamp(System.currentTimeMillis()).time
		
		println(timestamp)
		
	}
}
