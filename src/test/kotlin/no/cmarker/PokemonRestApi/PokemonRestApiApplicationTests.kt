package no.cmarker.PokemonRestApi

import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import junit.framework.Assert.assertEquals
import no.cmarker.PokemonRestApi.models.dto.PokemonDto
import org.hamcrest.CoreMatchers
import org.junit.Test
import org.springframework.http.HttpStatus.*

class PokemonRestApiTest : TestBase() {
	
	@Test
	fun testCleanDB() {
		
		val response = given().get().then()
			.statusCode(OK.value())
			.body("page.data.size()", CoreMatchers.equalTo(0))
			.and().extract().response()
		
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
			.statusCode(OK.value())
			.extract().jsonPath().getMap<String, Any>("page.data[0]")
		
		//assertEquals(resultList["id"], id1)
		assertEquals(resultList["name"], name)
		assertEquals(resultList["type"], type)
		assertEquals(resultList["imgUrl"], imgUrl)
	}
	
	@Test
	fun createMultipleTest() {
		
		
		createMultiple(8)
		
		assertResultSize(8)
		
	}
	
	@Test
	fun getAllByTypeTest() {
		
		assertResultSize(0)
		
		val number = 99
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		createPokemon(number, name, type, imgUrl)
		
		assertResultSize(1)
		
		given().param("type", "Grass")
			.get()
			.then()
			.statusCode(OK.value())
			.body("page.data.size()", CoreMatchers.equalTo(1))
		
		given().param("type", "Water")
			.get()
			.then()
			.statusCode(OK.value())
			.body("page.data.size()", CoreMatchers.equalTo(0))
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
		createPokemon(number2, name, type, imgUrl)
		
		assertResultSize(2)
		
		//deleting id1
		val resCode = given().param("id", id1).delete().then().extract().statusCode()
		
		// asserting
		given().get().then().body("page.data.id", CoreMatchers.not(CoreMatchers.containsString("" + id1)))
		assertResultSize(1)
		assertEquals(NO_CONTENT.value(), resCode)
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
		
		assertEquals(NO_CONTENT.value(), res.statusCode())
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
		
		val etag = given().param("id", id)
			.get()
			.then()
			.statusCode(OK.value())
			.extract().header("ETag")
			// The ETag is padded with "<etag>"
			.removeSurrounding("\"")
		
		// The id in param doesn't match the id in the req. body
		// Expected error code: 409
		val res1 = given()
			.header("If-Match", etag)
			.contentType(ContentType.JSON)
			.pathParam("id", id)
			.body(PokemonDto(99, updatedNumber, updatedName, updatedType, updatedImgUrl))
			.put("/id/{id}")
			.then()
			.extract()
		
		assertEquals(NOT_FOUND.value(), res1.statusCode())
		
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
			.statusCode(NO_CONTENT.value())
			.body("number", CoreMatchers.equalTo(updatedNumber))
	}
	
	@Test
	fun testEtag() {
		assertResultSize(0)
		
		val etag = RestAssured.given().accept(ContentType.JSON)
			.get()
			.then()
			.statusCode(OK.value())
			.header("ETag", CoreMatchers.notNullValue())
			.extract().header("ETag")
		
		
		given().accept(ContentType.JSON)
			.header("If-None-Match", etag)
			.get()
			.then()
			.statusCode(NOT_MODIFIED.value())
			.content(CoreMatchers.equalTo(""))
	}
	
	@Test
	fun testEtagAfterUpdate() {
		
		val number = 98
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		val id = createPokemon(number, name, type, imgUrl)
		
		val etag = given().accept(ContentType.JSON)
			.get()
			.then()
			.statusCode(OK.value())
			.header("ETag", CoreMatchers.notNullValue())
			.extract().header("ETag")
		
		given().accept(ContentType.JSON)
			.header("If-None-Match", etag)
			.get()
			.then()
			.statusCode(NOT_MODIFIED.value())
			.content(CoreMatchers.equalTo(""))
		
		// Updating entitiy to change the generated ETag
		
		given().contentType(ContentType.JSON)
			.pathParam("id", id)
			.body(PokemonDto(id, number, "updatedName", type, imgUrl))
			.put("/id/{id}")
		
		given().accept(ContentType.JSON)
			.header("If-None-Match", etag)
			.get()
			.then()
			.statusCode(OK.value())
			.header("ETag", CoreMatchers.notNullValue())
			.header("Etag", CoreMatchers.not(CoreMatchers.equalTo(etag)))
	}
	
	@Test
	fun testOptimisticLocking() {
		
		val number = 98
		val name = "Owly"
		val type = "Grass"
		val imgUrl = "defaultUrl"
		
		val id1 = createPokemon(number, name, type, imgUrl)
		
		val response = given().param("id", id1).get().then().extract().response()
		
		val etag = response.header("ETag")
		
		// Make the ETag invalid by simulating a modification made by someone else
		val updatedName = "Charichar"
		
		given()
			.contentType(ContentType.JSON)
			.pathParam("id", id1)
			.body(PokemonDto(id1, number, updatedName, type, imgUrl))
			.put("/id/{id}")
			.then()
			.statusCode(NO_CONTENT.value())
		
		// try to update with now invalid ETag
		val updatedName2 = "Longilong"
		given()
			.contentType(ContentType.JSON)
			.pathParam("id", id1)
			.header("If-Match", etag)
			.body(PokemonDto(id1, number, updatedName2, type, imgUrl))
			.put("/id/{id}")
			.then()
			.statusCode(PRECONDITION_FAILED.value())
		
	}
}