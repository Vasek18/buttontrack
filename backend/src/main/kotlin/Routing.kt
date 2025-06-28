package com.buttontrack

import com.buttontrack.dto.CreateButtonRequest
import com.buttontrack.dto.UpdateButtonRequest
import com.buttontrack.service.ButtonService
import com.buttontrack.service.AuthService
import com.buttontrack.plugins.AuthenticationPlugin
import com.buttontrack.plugins.getUserInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val idToken: String)

fun Application.configureRouting() {
    val buttonService = ButtonService()
    val authService = AuthService()

    routing {
        // Public auth endpoint
        post("/api/auth") {
            try {
                val request = call.receive<AuthRequest>()
                val userInfo = authService.verifyToken(request.idToken)
                if (userInfo != null) {
                    call.respond(HttpStatusCode.OK, userInfo)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
            }
        }

        // Protected routes
        install(AuthenticationPlugin) {}
        route("/api/buttons") {
            post {
                try {
                    val userInfo = call.getUserInfo()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))
                    val request = call.receive<CreateButtonRequest>()
                    
                    val validationErrors = request.validate()
                    if (validationErrors.isNotEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "error" to "Validation failed",
                            "details" to validationErrors
                        ))
                        return@post
                    }
                    
                    val button = buttonService.createButton(request, userInfo.id)
                    call.respond(HttpStatusCode.Created, button)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
                }
            }

            get {
                try {
                    val userInfo = call.getUserInfo()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))
                    val buttons = buttonService.getButtonsByUser(userInfo.id)
                    call.respond(buttons)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch buttons"))
                }
            }

            get("/{id}") {
                try {
                    val idStr = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing button ID")
                    )
                    val id = idStr.toInt()
                    val button = buttonService.getButton(id)
                    if (button != null) {
                        call.respond(button)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Button not found"))
                    }
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid button ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid button ID"))
                }
            }

            put("/{id}") {
                try {
                    val idStr = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing button ID")
                    )
                    val id = idStr.toInt()
                    val request = call.receive<UpdateButtonRequest>()
                    val button = buttonService.updateButton(id, request)
                    if (button != null) {
                        call.respond(button)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Button not found"))
                    }
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid button ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
                }
            }

            delete("/{id}") {
                try {
                    val idStr = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing button ID")
                    )
                    val id = idStr.toInt()
                    val deleted = buttonService.deleteButton(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Button not found"))
                    }
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid button ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid button ID"))
                }
            }
        }

        route("/api/press") {
            post("/{id}") {
                try {
                    val idStr = call.parameters["id"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing button ID")
                    )
                    val id = idStr.toInt()
                    val success = buttonService.pressButton(id)
                    if (success) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Button pressed successfully"))
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Button not found"))
                    }
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid button ID format"))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to press button"))
                }
            }
        }

        route("/api/stats") {
            get {
                try {
                    val userInfo = call.getUserInfo()
                        ?: return@get call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "User not authenticated"))
                    
                    val startTimestamp = call.request.queryParameters["start"]
                    val endTimestamp = call.request.queryParameters["end"]
                    
                    val stats = buttonService.getButtonPressStats(userInfo.id, startTimestamp, endTimestamp)
                    call.respond(HttpStatusCode.OK, stats)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch statistics"))
                }
            }
        }
    }
}
