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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertContains

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

}