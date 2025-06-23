package com.buttontrack

import com.buttontrack.dto.CreateButtonRequest
import com.buttontrack.dto.UpdateButtonRequest
import com.buttontrack.models.ButtonTable
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

class RoutingTest {

    private val testUserId = 1

    @BeforeEach
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(ButtonTable)
        }
    }

    @AfterEach
    fun teardown() {
        transaction {
            SchemaUtils.drop(ButtonTable)
        }
    }

    @Test
    fun `POST api buttons creates button successfully`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val response = client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                userId = testUserId,
                title = "Test Button",
                color = "#FF0000"
            ))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, testUserId.toString())
        assertContains(responseBody, "Test Button")
        assertContains(responseBody, "#FF0000")
    }

    @Test
    fun `POST api buttons returns 400 for invalid request`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val response = client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody("{\"invalid\": \"json\"}")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertContains(response.bodyAsText(), "Invalid request")
    }

    @Test
    fun `POST api buttons returns 400 for validation errors`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val response = client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                userId = 0,
                title = "",
                color = "invalid-color"
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "Validation failed")
        assertContains(responseBody, "userId must be a positive integer")
        assertContains(responseBody, "title cannot be empty")
        assertContains(responseBody, "color must be a valid hex color code")
    }

    @Test
    fun `GET api buttons requires userId parameter`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val response = client.get("/api/buttons")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertContains(response.bodyAsText(), "userId parameter is required")
    }

    @Test
    fun `GET api buttons returns user buttons successfully`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                userId = testUserId,
                title = "Button 1",
                color = "#FF0000"
            ))
        }

        client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                userId = testUserId,
                title = "Button 2",
                color = "#00FF00"
            ))
        }

        val response = client.get("/api/buttons?userId=$testUserId")

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "Button 1")
        assertContains(responseBody, "Button 2")
    }

    @Test
    fun `GET api buttons returns empty list for user with no buttons`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val response = client.get("/api/buttons?userId=$testUserId")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("[]", response.bodyAsText())
    }

    @Test
    fun `GET api buttons id returns button successfully`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val createResponse = client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                userId = testUserId,
                title = "Test Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        val response = client.get("/api/buttons/$buttonId")

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, buttonId)
        assertContains(responseBody, "Test Button")
    }

    @Test
    fun `GET api buttons id returns 404 for non-existent button`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val nonExistentId = "999"
        val response = client.get("/api/buttons/$nonExistentId")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `GET api buttons id returns 400 for invalid ID format`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val response = client.get("/api/buttons/invalid-id")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertContains(response.bodyAsText(), "Invalid button ID format")
    }

    @Test
    fun `PUT api buttons id updates button successfully`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val createResponse = client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                userId = testUserId,
                title = "Original Title",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        val response = client.put("/api/buttons/$buttonId") {
            contentType(ContentType.Application.Json)
            setBody(UpdateButtonRequest(
                title = "Updated Title",
                color = "#00FF00"
            ))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "Updated Title")
        assertContains(responseBody, "#00FF00")
    }

    @Test
    fun `PUT api buttons id returns 404 for non-existent button`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val nonExistentId = "999"
        val response = client.put("/api/buttons/$nonExistentId") {
            contentType(ContentType.Application.Json)
            setBody(UpdateButtonRequest(
                title = "Updated Title",
                color = "#00FF00"
            ))
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `PUT api buttons id returns 400 for invalid request`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val response = client.put("/api/buttons/invalid-uuid") {
            contentType(ContentType.Application.Json)
            setBody("{\"invalid\": \"json\"}")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertContains(response.bodyAsText(), "Invalid request")
    }

    @Test
    fun `DELETE api buttons id deletes button successfully`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val createResponse = client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                userId = testUserId,
                title = "Test Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        val response = client.delete("/api/buttons/$buttonId")

        assertEquals(HttpStatusCode.NoContent, response.status)

        val getResponse = client.get("/api/buttons/$buttonId")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `DELETE api buttons id returns 404 for non-existent button`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val nonExistentId = "999"
        val response = client.delete("/api/buttons/$nonExistentId")

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `DELETE api buttons id returns 400 for invalid ID format`() = testApplication {
        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting()
        }

        val response = client.delete("/api/buttons/invalid-id")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertContains(response.bodyAsText(), "Invalid button ID format")
    }
}