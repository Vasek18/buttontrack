package com.buttontrack

import com.buttontrack.dto.CreateButtonRequest
import com.buttontrack.dto.UpdateButtonRequest
import com.buttontrack.service.ButtonService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val buttonService = ButtonService()

    routing {
        route("/api/buttons") {
            post {
                try {
                    val request = call.receive<CreateButtonRequest>()
                    
                    val validationErrors = request.validate()
                    if (validationErrors.isNotEmpty()) {
                        call.respond(HttpStatusCode.BadRequest, mapOf(
                            "error" to "Validation failed",
                            "details" to validationErrors
                        ))
                        return@post
                    }
                    
                    val button = buttonService.createButton(request)
                    call.respond(HttpStatusCode.Created, button)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
                }
            }

            get {
                try {
                    val userIdStr = call.request.queryParameters["userId"] 
                        ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "userId parameter is required"))
                    val userId = userIdStr.toInt()
                    val buttons = buttonService.getButtonsByUser(userId)
                    call.respond(buttons)
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid userId format"))
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
    }
}
