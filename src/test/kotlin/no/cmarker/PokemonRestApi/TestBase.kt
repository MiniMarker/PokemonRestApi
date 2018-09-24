package no.cmarker.PokemonRestApi

import io.restassured.RestAssured
import io.restassured.http.ContentType
import no.cmarker.PokemonRestApi.dto.PokemonDto
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
				.`as`(Array<PokemonDto>::class.java) //need to escape the as keyword!
				.toList()
		
		list.stream().forEach {
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
				.body("size()", CoreMatchers.equalTo(0))
	}
}