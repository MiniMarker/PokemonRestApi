package no.cmarker.PokemonRestApi

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import junit.framework.Assert.assertEquals
import no.cmarker.PokemonRestApi.dto.PokemonDto
import no.cmarker.PokemonRestApi.dto.ResponseDto
import org.hamcrest.CoreMatchers
import org.junit.Test

class PokemonRestApiTest : TestBase() {
	
	@Test
	fun testCleanDB() {
		
		val response = RestAssured.given().get().then()
				.statusCode(200)
				.body("data.size()", CoreMatchers.equalTo(0))
				.and().extract().response()
		
		println(response.asString())
		
	}
	
	@Test
	fun createAndGetById() {
		
		assertResultSize(0)
		
		val number = 99
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		val id1 = createPokemon(number, name, type, imgUrl)
		
		assertResultSize(1)
		
		val resultList = given().param("id", id1)
				.get()
				.then()
				.statusCode(200)
				.extract()
				.`as`(ResponseDto::class.java).data!!
				//.body("data.id", CoreMatchers.equalTo(id1.toInt()))
				//.body("data.name", CoreMatchers.equalTo(name))
				//.body("data.type", CoreMatchers.equalTo(type))
				//.body("data.number", CoreMatchers.equalTo(number))
				//.body("data.imgUrl", CoreMatchers.equalTo(imgUrl))
		
		assertEquals(id1, resultList.get(0).id)
		assertEquals(name, resultList.get(0).name)
		assertEquals(type, resultList.get(0).type)
		assertEquals(imgUrl, resultList.get(0).imgUrl)
		
	}
	
	@Test
	fun getAllByTypeTest() {
		
		assertResultSize(0)
		
		val number = 99
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		val id1 = createPokemon(number, name, type, imgUrl)
		
		assertResultSize(1)
		
		given().param("type", "Grass")
				.get()
				.then()
				.statusCode(200)
				.body("data.size()", CoreMatchers.equalTo(1))
		
		given().param("type", "Water")
				.get()
				.then()
				.statusCode(200)
				.body("data.size()", CoreMatchers.equalTo(0))
	}
	
	@Test
	fun deleteEntityTest() {
		
		assertResultSize(0)
		
		val number1 = 98
		val number2 = 99
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		val id1 = createPokemon(number1, name, type, imgUrl)
		val id2 = createPokemon(number2, name, type, imgUrl)
		
		assertResultSize(2)
		
		//deleting id1
		val resCode = given().param("id", id1).delete().then().extract().statusCode()
		
		// asserting
		given().get().then().body("id", CoreMatchers.not(CoreMatchers.containsString("" + id1)))
		assertResultSize(1)
		assertEquals(204, resCode)
	}
	
	@Test
	fun putPokemonTest() {
		
		assertResultSize(0)
		
		val number = 98
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		val id = createPokemon(number, name, type, imgUrl)
		
		assertResultSize(1)
		
		val updatedName = "UpdatedName"
		val updatedNumber = 99
		val updatedType = "UpdatedType"
		val updatedImgUrl = "UpdatedUrl"
		
		// The id is not a Long, the code will fail at
		// Expected error code: 500
		val res = given()
				.contentType(ContentType.JSON)
				.pathParam("id", id)
				.body(PokemonDto(id, updatedNumber, updatedName, updatedType, updatedImgUrl))
				.put("/id/{id}")
				.then()
				.extract()
		
		assertEquals(204, res.statusCode())
		
	}
	
	@Test
	fun putPokemonFailTest() {
		
		assertResultSize(0)
		
		val number = 98
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		val id = createPokemon(number, name, type, imgUrl)
		
		assertResultSize(1)
		
		val updatedName = "UpdatedName"
		val updatedNumber = 99
		val updatedType = "UpdatedType"
		val updatedImgUrl = "UpdatedUrl"
		
		// The id in param doesn't match the id in the req. body
		// Expected error code: 409
		val res1 = given()
				.contentType(ContentType.JSON)
				.pathParam("id", id)
				.body(PokemonDto(99, updatedNumber, updatedName, updatedType, imgUrl))
				.put("/id/{id}")
				.then()
				.extract()
		
		assertEquals(409, res1.statusCode())
		
		/*
		// THIS IS NOT POSSIBLE AT THIS MOMENT BECAUSE THE ID IS A LONG!
		// IF ANDREA CHANGES THE ID TO A STRING. THEN THIS WILL WORK
		val reqBody: JSONObject = JSONObject()
				.put("id", id)
				.put("name", updatedName)
				.put("updatedNumber", updatedNumber)
				.put("type", updatedType)
		
		
		// The id is not a Long
		// Expected error code: 404
		val res2 = given()
				.contentType(ContentType.JSON)
				.pathParam("id", id)
				.body(reqBody.toString())
				.put("/id/{id}")
				.then()
				.extract()
		
		assertEquals(204, res2.statusCode())
		
		println(res2.statusCode())
		*/
	}
	
	@Test
	fun patchPokemonTest() {
		
		assertResultSize(0)
		
		val number = 98
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		val id = createPokemon(number, name, type, imgUrl)
		
		assertResultSize(1)
		
		val updatedNumber = 222
		
		given().contentType("application/merge-patch+json")
				.pathParam("id", id)
				.body("{'number': $updatedNumber}")
				.then()
				.statusCode(204)
				.body("number", CoreMatchers.equalTo(updatedNumber))
	}
	
	
	/*
		HELPING METHODS!
	 */
	
	fun createPokemon(number: Int, name: String, type: String, imgUrl: String): Long {
		
		val dto = PokemonDto(null, number, name, type, imgUrl)
		
		return given().contentType(ContentType.JSON)
				.body(dto)        // send the DTO as POST req body
				.post()
				.then()
				.statusCode(201)
				.extract().asString().toLong()
	}
	
	fun assertResultSize(size: Int){
		given().get().then().statusCode(200).body("data.size()", CoreMatchers.equalTo(size))
	}
	
}