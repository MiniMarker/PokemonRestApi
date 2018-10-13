package no.cmarker.PokemonRestApi

import io.restassured.RestAssured
import io.restassured.http.ContentType
import no.cmarker.PokemonRestApi.models.dto.PokemonDto
import no.cmarker.PokemonRestApi.models.dto.ResponseDto
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner

/**
 * @author Christian Marker on 24/09/2018 at 11:46.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(
		classes = [(PokemonRestApiApplication::class)],
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class TestBase {
	
	@LocalServerPort
	protected var port = 0
	
	@Before
	@After
	fun clean() {
		
		// I use RestAssured to minimize boilerplate code.
		// Here i set the base settings for the tests
		RestAssured.baseURI = "http://localhost"       	// defining the base URL
		RestAssured.port = port                       	// setting the port to the Random Generated port that Springboot gives us
		RestAssured.basePath = "/pokemon"				// defining the base URL path
		
		RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()
		
		val list = RestAssured.given().accept(ContentType.JSON).get()
				.then()
				.statusCode(200)
				.extract()
				.`as`(ResponseDto::class.java)
				
		
		list.page!!.data.stream().forEach {
			RestAssured.given()
					.param("id", it.id)
					.delete()
					.then()
					.statusCode(204)
		}
		
		RestAssured.given()
				.get()
				.then()
				.statusCode(200)
				.body("page.data.size()", CoreMatchers.equalTo(0))
	}
	
	/*
		HELPING METHODS!
	 */
	
	fun createPokemon(number: Int, name: String, type: String, imgUrl: String): Long {
		
		val dto = PokemonDto(null, number, name, type, imgUrl)
		
		return RestAssured.given().contentType(ContentType.JSON)
				.body(dto)        // send the DTO as POST req body
				.post()
				.then()
				.statusCode(201)
				.extract()
				.jsonPath().getLong("page.data[0].id")
	}
	
	fun createMultiple(n: Int) {
		
		val name = "defaultName"
		val type = "defaultType"
		val imgUrl = "defaultUrl"
		
		for (i in 1..n) {
			createPokemon(i, name, type, imgUrl)
		}
		
		RestAssured.given().get().then().extract().body().jsonPath().prettyPrint()
		
	}
	
	fun assertResultSize(size: Int){
		RestAssured.given().get().then().statusCode(200).body("page.data.size()", CoreMatchers.equalTo(size))
	}
}