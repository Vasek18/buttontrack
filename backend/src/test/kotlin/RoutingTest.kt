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
import io.ktor.server.config.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains
import io.ktor.util.*

// Test authentication plugin that reads session data from headers
class TestAuthenticationPlugin {
    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Unit, TestAuthenticationPlugin> {
        override val key = AttributeKey<TestAuthenticationPlugin>("TestAuthentication")
        
        override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit): TestAuthenticationPlugin {
            val plugin = TestAuthenticationPlugin()
            
            pipeline.intercept(ApplicationCallPipeline.Call) {
                // Skip authentication for the auth endpoint
                if (call.request.path() == "/api/auth" || call.request.path() == "/api/logout") {
                    return@intercept
                }
                
                // Check for test session data in headers
                val sessionHeader = call.request.headers["X-Test-Session-Data"]
                if (sessionHeader != null) {
                    try {
                        val session = Json.decodeFromString(UserSession.serializer(), sessionHeader)
                        // Set the session so the normal auth plugin can use it
                        call.sessions.set(session)
                        
                        // Also set user info directly for the controllers
                        val userInfo = UserInfo(
                            id = session.userId,
                            email = session.email,
                            name = session.name
                        )
                        call.attributes.put(UserInfoKey, userInfo)
                        return@intercept
                    } catch (e: Exception) {
                        // Invalid session data, fall through to normal auth
                    }
                }
                
                // If no test session, check normal session
                val session = call.sessions.get<UserSession>()
                if (session == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "No valid session"))
                    return@intercept finish()
                }
                
                // Convert session to UserInfo
                val userInfo = UserInfo(
                    id = session.userId,
                    email = session.email,
                    name = session.name
                )
                call.attributes.put(UserInfoKey, userInfo)
            }
            
            return plugin
        }
    }
}

class RoutingTest {

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
        environment {
            config = MapApplicationConfig().apply {
                put("ktor.deployment.googleClientId", "test-google-client-id")
            }
        }
        application {
            install(ContentNegotiation) {
                json()
            }
            install(Sessions) {
                cookie<UserSession>("SESSION")
            }
            
            // Install test authentication plugin that reads from headers
            install(TestAuthenticationPlugin)
            
            configureRouting()
        }
    }

    @Test
    fun `POST api buttons returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.post("/api/buttons") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Test Button","color":"#FF0000"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "No valid session")
    }

    @Test
    fun `GET api buttons returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.get("/api/buttons")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "No valid session")
    }

    @Test
    fun `GET api stats returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.get("/api/stats")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "No valid session")
    }

    @Test
    fun `GET api buttons id returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.get("/api/buttons/1")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "No valid session")
    }

    @Test
    fun `PUT api buttons id returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.put("/api/buttons/1") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Test Button","color":"#FF0000"}""")
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "No valid session")
    }

    @Test
    fun `DELETE api buttons id returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.delete("/api/buttons/1")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "No valid session")
    }

    @Test
    fun `POST api press id returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.post("/api/press/1")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "No valid session")
    }

    @Test
    fun `GET api me returns 401 when not authenticated`() = testApplication {
        setupTestApp()

        val response = client.get("/api/me")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertContains(response.bodyAsText(), "No valid session")
    }

    // 403 Forbidden tests - ownership verification
    
    @Test
    fun `GET api buttons id returns 403 when user tries to access another users button`() = testApplication {
        setupTestApp()
        
        // Create button for user 1
        val buttonId = createTestButtonForUser(1)
        
        // Try to access it as user 2
        val session2 = UserSession(userId = 2, email = "user2@test.com", name = "User 2")
        
        val response = client.get("/api/buttons/$buttonId") {
            withAuthenticatedSession(session2)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertContains(response.bodyAsText(), "You can only access your own buttons")
    }

    @Test
    fun `PUT api buttons id returns 403 when user tries to edit another users button`() = testApplication {
        setupTestApp()
        
        // Create button for user 1
        val buttonId = createTestButtonForUser(1)
        
        // Try to edit it as user 2
        val session2 = UserSession(userId = 2, email = "user2@test.com", name = "User 2")
        
        val response = client.put("/api/buttons/$buttonId") {
            withAuthenticatedSession(session2)
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Hacked Button","color":"#000000"}""")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertContains(response.bodyAsText(), "You can only edit your own buttons")
    }

    @Test
    fun `DELETE api buttons id returns 403 when user tries to delete another users button`() = testApplication {
        setupTestApp()
        
        // Create button for user 1
        val buttonId = createTestButtonForUser(1)
        
        // Try to delete it as user 2
        val session2 = UserSession(userId = 2, email = "user2@test.com", name = "User 2")
        
        val response = client.delete("/api/buttons/$buttonId") {
            withAuthenticatedSession(session2)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertContains(response.bodyAsText(), "You can only delete your own buttons")
    }

    @Test
    fun `POST api press id returns 403 when user tries to press another users button`() = testApplication {
        setupTestApp()
        
        // Create button for user 1
        val buttonId = createTestButtonForUser(1)
        
        // Try to press it as user 2
        val session2 = UserSession(userId = 2, email = "user2@test.com", name = "User 2")
        
        val response = client.post("/api/press/$buttonId") {
            withAuthenticatedSession(session2)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertContains(response.bodyAsText(), "You can only press your own buttons")
    }

    @Test
    fun `GET api buttons id returns 404 when button does not exist`() = testApplication {
        setupTestApp()
        
        val session = UserSession(userId = 1, email = "user1@test.com", name = "User 1")
        
        val response = client.get("/api/buttons/99999") {
            withAuthenticatedSession(session)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `PUT api buttons id returns 404 when button does not exist`() = testApplication {
        setupTestApp()
        
        val session = UserSession(userId = 1, email = "user1@test.com", name = "User 1")
        
        val response = client.put("/api/buttons/99999") {
            withAuthenticatedSession(session)
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Non-existent Button","color":"#000000"}""")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `DELETE api buttons id returns 404 when button does not exist`() = testApplication {
        setupTestApp()
        
        val session = UserSession(userId = 1, email = "user1@test.com", name = "User 1")
        
        val response = client.delete("/api/buttons/99999") {
            withAuthenticatedSession(session)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `POST api press id returns 404 when button does not exist`() = testApplication {
        setupTestApp()
        
        val session = UserSession(userId = 1, email = "user1@test.com", name = "User 1")
        
        val response = client.post("/api/press/99999") {
            withAuthenticatedSession(session)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertContains(response.bodyAsText(), "Button not found")
    }

    @Test
    fun `GET api buttons id succeeds when user accesses their own button`() = testApplication {
        setupTestApp()
        
        // Create button for user 1
        val buttonId = createTestButtonForUser(1)
        
        // Access it as user 1 (owner)
        val session = UserSession(userId = 1, email = "user1@test.com", name = "User 1")
        
        val response = client.get("/api/buttons/$buttonId") {
            withAuthenticatedSession(session)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "Test Button")
        assertContains(responseBody, "\"id\":$buttonId")
    }

    @Test
    fun `PUT api buttons id succeeds when user edits their own button`() = testApplication {
        setupTestApp()
        
        // Create button for user 1
        val buttonId = createTestButtonForUser(1)
        
        // Edit it as user 1 (owner)
        val session = UserSession(userId = 1, email = "user1@test.com", name = "User 1")
        
        val response = client.put("/api/buttons/$buttonId") {
            withAuthenticatedSession(session)
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Updated Button","color":"#00FF00"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        assertContains(responseBody, "Updated Button")
        assertContains(responseBody, "#00FF00")
    }

    @Test
    fun `DELETE api buttons id succeeds when user deletes their own button`() = testApplication {
        setupTestApp()
        
        // Create button for user 1
        val buttonId = createTestButtonForUser(1)
        
        // Delete it as user 1 (owner)
        val session = UserSession(userId = 1, email = "user1@test.com", name = "User 1")
        
        val response = client.delete("/api/buttons/$buttonId") {
            withAuthenticatedSession(session)
        }

        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `POST api press id succeeds when user presses their own button`() = testApplication {
        setupTestApp()
        
        // Create button for user 1
        val buttonId = createTestButtonForUser(1)
        
        // Press it as user 1 (owner)
        val session = UserSession(userId = 1, email = "user1@test.com", name = "User 1")
        
        val response = client.post("/api/press/$buttonId") {
            withAuthenticatedSession(session)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "Button pressed successfully")
    }

    @Test
    fun `GET api buttons only returns buttons owned by authenticated user`() = testApplication {
        setupTestApp()
        
        // Create buttons for different users
        val user1ButtonId = createTestButtonForUser(1)
        val user2ButtonId = createTestButtonForUser(2)
        
        // Request as user 1
        val session = UserSession(userId = 1, email = "user1@test.com", name = "User 1")
        
        val response = client.get("/api/buttons") {
            withAuthenticatedSession(session)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        
        // Should contain user 1's button
        assertContains(responseBody, "\"id\":$user1ButtonId")
        // Should NOT contain user 2's button
        assert(!responseBody.contains("\"id\":$user2ButtonId")) {
            "Response should not contain other user's buttons"
        }
    }

    // Helper functions

    private suspend fun ApplicationTestBuilder.createTestButtonForUser(userId: Int): Int {
        val session = UserSession(userId = userId, email = "user$userId@test.com", name = "User $userId")
        
        val response = client.post("/api/buttons") {
            withAuthenticatedSession(session)
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Test Button","color":"#FF0000"}""")
        }
        
        assertEquals(HttpStatusCode.Created, response.status)
        val responseBody = response.bodyAsText()
        
        // Extract button ID from response
        val idMatch = Regex("\"id\":(\\d+)").find(responseBody)
        return idMatch?.groupValues?.get(1)?.toInt() 
            ?: throw AssertionError("Could not extract button ID from response: $responseBody")
    }
    
    private fun HttpRequestBuilder.withAuthenticatedSession(session: UserSession) {
        // Set session cookie for authentication
        // Note: In real Ktor testing, we need to properly configure sessions
        // For now, this creates a placeholder that would work with custom auth setup
        cookie("SESSION", session.sessionId)
        // Store session data in a way the test can access it
        header("X-Test-Session-Data", Json.encodeToString(UserSession.serializer(), session))
    }

}