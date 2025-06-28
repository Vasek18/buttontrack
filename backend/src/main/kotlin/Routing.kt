package com.buttontrack

import com.buttontrack.dto.CreateButtonRequest
import com.buttontrack.dto.UpdateButtonRequest
import com.buttontrack.service.ButtonService
import com.buttontrack.service.AuthService
import com.buttontrack.service.UserSession
import com.buttontrack.plugins.AuthenticationPlugin
import com.buttontrack.plugins.getUserInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val idToken: String)

fun Application.configureRouting() {
    val buttonService = ButtonService()
    val authService = AuthService()

    routing {
        // Install authentication plugin globally, but it will skip /api/auth
        install(AuthenticationPlugin) {}

        route("/api") {
            // Public auth endpoint
            post("/auth") {
                try {
                    val request = call.receive<AuthRequest>()
                    val userSession = authService.verifyGoogleTokenAndCreateSession(request.idToken)
                    if (userSession != null) {
                        call.sessions.set(userSession)
                        call.respond(HttpStatusCode.OK, mapOf(
                            "id" to userSession.userId,
                            "email" to userSession.email,
                            "name" to userSession.name
                        ))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
                }
            }
            
            // Logout endpoint
            post("/logout") {
                call.sessions.clear<UserSession>()
                call.respond(HttpStatusCode.OK, mapOf("message" to "Logged out successfully"))
            }

            route("/buttons") {
                post {
                    try {
                        val userInfo = call.getUserInfo()
                            ?: return@post call.respond(
                                HttpStatusCode.Unauthorized,
                                mapOf("error" to "User not authenticated")
                            )
                        val request = call.receive<CreateButtonRequest>()

                        val validationErrors = request.validate()
                        if (validationErrors.isNotEmpty()) {
                            call.respond(
                                HttpStatusCode.BadRequest, mapOf(
                                    "error" to "Validation failed",
                                    "details" to validationErrors
                                )
                            )
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
                            ?: return@get call.respond(
                                HttpStatusCode.Unauthorized,
                                mapOf("error" to "User not authenticated")
                            )
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

            route("/press") {
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

            route("/stats") {
                get {
                    try {
                        val userInfo = call.getUserInfo()
                            ?: return@get call.respond(
                                HttpStatusCode.Unauthorized,
                                mapOf("error" to "User not authenticated")
                            )

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
}
