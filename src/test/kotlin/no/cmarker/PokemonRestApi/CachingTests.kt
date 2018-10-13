package no.cmarker.PokemonRestApi

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import no.cmarker.PokemonRestApi.models.dto.PokemonDto
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.junit.Test
import org.springframework.http.HttpStatus

/**
 * @author Christian Marker on 13/10/2018 at 11:02.
 */
class CachingTests : TestBase() {
	
	@Test
	fun testEtag() {
		assertResultSize(0);
		
		val etag = RestAssured.given().accept(ContentType.JSON)
				.get()
				.then()
				.statusCode(200)
				.header("ETag", CoreMatchers.notNullValue())
				.extract().header("ETag")
		
		
		RestAssured.given().accept(ContentType.JSON)
				.header("If-None-Match", etag)
				.get()
				.then()
				.statusCode(304)
				.content(CoreMatchers.equalTo(""))
	}
	
	@Test
	fun testEtagAfterUpdate(){
		
		val number = 98
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		val id = createPokemon(number, name, type, imgUrl)
		
		val etag = given().accept(ContentType.JSON)
				.get()
				.then()
				.statusCode(200)
				.header("ETag", CoreMatchers.notNullValue())
				.extract().header("ETag")
		
		given().accept(ContentType.JSON)
				.header("If-None-Match", etag)
				.get()
				.then()
				.statusCode(304)
				.content(CoreMatchers.equalTo(""))
		
		// Updating entitiy to change the generated ETag
		val updatedEtag = given().contentType(ContentType.JSON)
				.pathParam("id", id)
				.body(PokemonDto(id, number, "updatedName", type, imgUrl))
				.put("/id/{id}")
		
		given().accept(ContentType.JSON)
				.header("If-None-Match", etag)
				.get()
				.then()
				.statusCode(200)
				.header("ETag", notNullValue())
				.header("Etag", not(equalTo(etag)))
				
	}
	
}