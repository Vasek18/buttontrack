package com.buttontrack

import com.buttontrack.dto.CreateButtonRequest
import com.buttontrack.dto.UpdateButtonRequest
import com.buttontrack.models.ButtonTable
import com.buttontrack.models.ButtonPressTable
import com.buttontrack.models.UserTable
import com.buttontrack.models.UserAuthTable
import com.buttontrack.service.UserInfo
import com.buttontrack.service.UserSession
import com.buttontrack.plugins.UserInfoKey
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.sessions.*
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
    private val testUserEmail = "test@example.com"
    private val testUserName = "Test User"
    private val testUserSession = UserSession(testUserId, testUserEmail, testUserName)

    @BeforeEach
    fun setup() {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
        transaction {
            SchemaUtils.create(UserTable, UserAuthTable, ButtonTable, ButtonPressTable)
        }
    }

    @AfterEach
    fun teardown() {
        transaction {
            SchemaUtils.drop(ButtonPressTable, ButtonTable, UserAuthTable, UserTable)
        }
    }

    private fun ApplicationTestBuilder.setupTestApp() {
        application {
            install(ContentNegotiation) {
                json()
            }
            install(Sessions) {
                cookie<UserSession>("SESSION")
            }
            
            // Install a test authentication interceptor that sets user info from session
            intercept(ApplicationCallPipeline.Call) {
                val session = call.sessions.get<UserSession>()
                if (session != null) {
                    val userInfo = UserInfo(
                        id = session.userId,
                        email = session.email,
                        name = session.name
                    )
                    call.attributes.put(UserInfoKey, userInfo)
                }
            }
            
            configureRouting()
        }
    }

    private fun HttpRequestBuilder.authenticateAs(userSession: UserSession) {
        // Set session cookie to authenticate as the given user
        val encodedSession = java.util.Base64.getEncoder().encodeToString(
            Json.encodeToString(UserSession.serializer(), userSession).toByteArray()
        )
        header("Cookie", "SESSION=$encodedSession")
    }

    @Test
    fun `POST api buttons creates button successfully`() = testApplication {
        setupTestApp()

        val response = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
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
    fun `POST api buttons returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Test Button",
                color = "#FF0000"
            ))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "User not authenticated")
    }

    @Test
    fun `POST api buttons returns 400 for validation errors`() = testApplication {
        setupTestApp()

        val response = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "",
                color = "invalid-color"
            ))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "Validation failed")
        assertContains(responseBody, "title cannot be empty")
        assertContains(responseBody, "color must be a valid hex color code")
    }

    @Test
    fun `GET api buttons returns user buttons successfully`() = testApplication {
        setupTestApp()

        client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Button 1",
                color = "#FF0000"
            ))
        }

        client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Button 2",
                color = "#00FF00"
            ))
        }

        val response = client.get("/api/buttons") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "Button 1")
        assertContains(responseBody, "Button 2")
    }

    @Test
    fun `GET api buttons returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.get("/api/buttons")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "User not authenticated")
    }

    @Test
    fun `GET api buttons id returns button successfully when user owns it`() = testApplication {
        setupTestApp()

        val createResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Test Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        val response = client.get("/api/buttons/$buttonId") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, buttonId)
        assertContains(responseBody, "Test Button")
    }

    @Test
    fun `GET api buttons id returns 404 for non-existent button`() = testApplication {
        setupTestApp()

        val nonExistentId = "999"
        val response = client.get("/api/buttons/$nonExistentId") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `PUT api buttons id updates button successfully when user owns it`() = testApplication {
        setupTestApp()

        val createResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Original Title",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        val response = client.put("/api/buttons/$buttonId") {
            authenticateAs(testUserSession)
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
        setupTestApp()

        val nonExistentId = "999"
        val response = client.put("/api/buttons/$nonExistentId") {
            authenticateAs(testUserSession)
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
    fun `DELETE api buttons id deletes button successfully when user owns it`() = testApplication {
        setupTestApp()

        val createResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Test Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        val response = client.delete("/api/buttons/$buttonId") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.NoContent, response.status)

        val getResponse = client.get("/api/buttons/$buttonId") {
            authenticateAs(testUserSession)
        }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `DELETE api buttons id returns 404 for non-existent button`() = testApplication {
        setupTestApp()

        val nonExistentId = "999"
        val response = client.delete("/api/buttons/$nonExistentId") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `POST api press id records button press successfully when user owns button`() = testApplication {
        setupTestApp()

        val createResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Test Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        val response = client.post("/api/press/$buttonId") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "Button pressed successfully")
    }

    @Test
    fun `POST api press id returns 404 for non-existent button`() = testApplication {
        setupTestApp()

        val nonExistentId = "999"
        val response = client.post("/api/press/$nonExistentId") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `GET api stats returns stats successfully`() = testApplication {
        setupTestApp()

        val createResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Test Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        client.post("/api/press/$buttonId") {
            authenticateAs(testUserSession)
        }

        val response = client.get("/api/stats") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "buttonStats")
        assertContains(responseBody, "Test Button")
    }

    @Test
    fun `GET api stats returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.get("/api/stats")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "User not authenticated")
    }

    // ============================================================================
    // SECURITY TESTS: Hacking Attempts (403 Forbidden)
    // ============================================================================

    @Test
    fun `GET api buttons id returns 403 when user tries to access another user's button`() = testApplication {
        setupTestApp()
        
        val otherUserId = 2
        val otherUserEmail = "hacker@example.com"
        val otherUserName = "Hacker User"
        val hackerSession = UserSession(otherUserId, otherUserEmail, otherUserName)

        // User 1 creates a button
        val createResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Private Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        // Hacker (User 2) tries to access User 1's button
        val response = client.get("/api/buttons/$buttonId") {
            authenticateAs(hackerSession)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertContains(response.bodyAsText(), "You can only access your own buttons")
    }

    @Test
    fun `PUT api buttons id returns 403 when user tries to edit another user's button`() = testApplication {
        setupTestApp()
        
        val otherUserId = 2
        val otherUserEmail = "hacker@example.com"
        val otherUserName = "Hacker User"
        val hackerSession = UserSession(otherUserId, otherUserEmail, otherUserName)

        // User 1 creates a button
        val createResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Victim Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        // Hacker (User 2) tries to edit User 1's button
        val response = client.put("/api/buttons/$buttonId") {
            authenticateAs(hackerSession)
            contentType(ContentType.Application.Json)
            setBody(UpdateButtonRequest(
                title = "HACKED BUTTON",
                color = "#000000"
            ))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertContains(response.bodyAsText(), "You can only edit your own buttons")
        
        // Verify the button was NOT modified
        val getResponse = client.get("/api/buttons/$buttonId") {
            authenticateAs(testUserSession)
        }
        assertEquals(HttpStatusCode.OK, getResponse.status)
        assertContains(getResponse.bodyAsText(), "Victim Button") // Original title intact
        assertContains(getResponse.bodyAsText(), "#FF0000") // Original color intact
    }

    @Test
    fun `DELETE api buttons id returns 403 when user tries to delete another user's button`() = testApplication {
        setupTestApp()
        
        val otherUserId = 2
        val otherUserEmail = "hacker@example.com"
        val otherUserName = "Hacker User"
        val hackerSession = UserSession(otherUserId, otherUserEmail, otherUserName)

        // User 1 creates a button
        val createResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Protected Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        // Hacker (User 2) tries to delete User 1's button
        val response = client.delete("/api/buttons/$buttonId") {
            authenticateAs(hackerSession)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertContains(response.bodyAsText(), "You can only delete your own buttons")
        
        // Verify the button still exists and was NOT deleted
        val getResponse = client.get("/api/buttons/$buttonId") {
            authenticateAs(testUserSession)
        }
        assertEquals(HttpStatusCode.OK, getResponse.status)
        assertContains(getResponse.bodyAsText(), "Protected Button")
    }

    @Test
    fun `POST api press id returns 403 when user tries to press another user's button`() = testApplication {
        setupTestApp()
        
        val otherUserId = 2
        val otherUserEmail = "hacker@example.com"
        val otherUserName = "Hacker User"
        val hackerSession = UserSession(otherUserId, otherUserEmail, otherUserName)

        // User 1 creates a button
        val createResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "Secret Button",
                color = "#FF0000"
            ))
        }

        val buttonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(createResponse.bodyAsText())
        val buttonId = buttonData["id"]!!.toString().replace("\"", "")

        // Hacker (User 2) tries to press User 1's button
        val response = client.post("/api/press/$buttonId") {
            authenticateAs(hackerSession)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertContains(response.bodyAsText(), "You can only press your own buttons")
    }

    @Test
    fun `GET api buttons should not leak other users' buttons in response`() = testApplication {
        setupTestApp()
        
        val otherUserId = 2
        val otherUserEmail = "user2@example.com"
        val otherUserName = "User Two"
        val user2Session = UserSession(otherUserId, otherUserEmail, otherUserName)

        // User 1 creates buttons
        client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "User1 Private Button",
                color = "#FF0000"
            ))
        }

        // User 2 creates buttons
        client.post("/api/buttons") {
            authenticateAs(user2Session)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "User2 Secret Button",
                color = "#00FF00"
            ))
        }

        // User 1 fetches their buttons - should NOT see User 2's buttons
        val user1Response = client.get("/api/buttons") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.OK, user1Response.status)
        val user1Buttons = user1Response.bodyAsText()
        assertContains(user1Buttons, "User1 Private Button")
        kotlin.test.assertFalse(user1Buttons.contains("User2 Secret Button"), 
            "User 1 should not see User 2's buttons")

        // User 2 fetches their buttons - should NOT see User 1's buttons
        val user2Response = client.get("/api/buttons") {
            authenticateAs(user2Session)
        }

        assertEquals(HttpStatusCode.OK, user2Response.status)
        val user2Buttons = user2Response.bodyAsText()
        assertContains(user2Buttons, "User2 Secret Button")
        kotlin.test.assertFalse(user2Buttons.contains("User1 Private Button"), 
            "User 2 should not see User 1's buttons")
    }

    @Test
    fun `GET api stats should not leak other users' statistics`() = testApplication {
        setupTestApp()
        
        val otherUserId = 2
        val otherUserEmail = "user2@example.com"
        val otherUserName = "User Two"
        val user2Session = UserSession(otherUserId, otherUserEmail, otherUserName)

        // User 1 creates and presses a button
        val user1CreateResponse = client.post("/api/buttons") {
            authenticateAs(testUserSession)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "User1 Activity Button",
                color = "#FF0000"
            ))
        }

        val user1ButtonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(user1CreateResponse.bodyAsText())
        val user1ButtonId = user1ButtonData["id"]!!.toString().replace("\"", "")

        client.post("/api/press/$user1ButtonId") {
            authenticateAs(testUserSession)
        }

        // User 2 creates and presses a button
        val user2CreateResponse = client.post("/api/buttons") {
            authenticateAs(user2Session)
            contentType(ContentType.Application.Json)
            setBody(CreateButtonRequest(
                title = "User2 Activity Button",
                color = "#00FF00"
            ))
        }

        val user2ButtonData = Json.decodeFromString<Map<String, kotlinx.serialization.json.JsonElement>>(user2CreateResponse.bodyAsText())
        val user2ButtonId = user2ButtonData["id"]!!.toString().replace("\"", "")

        client.post("/api/press/$user2ButtonId") {
            authenticateAs(user2Session)
        }

        // User 1 fetches stats - should NOT see User 2's stats
        val user1StatsResponse = client.get("/api/stats") {
            authenticateAs(testUserSession)
        }

        assertEquals(HttpStatusCode.OK, user1StatsResponse.status)
        val user1Stats = user1StatsResponse.bodyAsText()
        assertContains(user1Stats, "User1 Activity Button")
        kotlin.test.assertFalse(user1Stats.contains("User2 Activity Button"), 
            "User 1 should not see User 2's button stats")

        // User 2 fetches stats - should NOT see User 1's stats
        val user2StatsResponse = client.get("/api/stats") {
            authenticateAs(user2Session)
        }

        assertEquals(HttpStatusCode.OK, user2StatsResponse.status)
        val user2Stats = user2StatsResponse.bodyAsText()
        assertContains(user2Stats, "User2 Activity Button")
        kotlin.test.assertFalse(user2Stats.contains("User1 Activity Button"), 
            "User 2 should not see User 1's button stats")
    }
}