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
                    val button = buttonService.createButton(request)
                    call.respond(HttpStatusCode.Created, button)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
                }
            }

            get {
                try {
                    val userId = call.request.queryParameters["userId"] 
                        ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "userId parameter is required"))
                    val buttons = buttonService.getButtonsByUser(userId)
                    call.respond(buttons)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to fetch buttons"))
                }
            }

            get("/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing button ID")
                    )
                    val button = buttonService.getButton(id)
                    if (button != null) {
                        call.respond(button)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Button not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid button ID"))
                }
            }

            put("/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing button ID")
                    )
                    val request = call.receive<UpdateButtonRequest>()
                    val button = buttonService.updateButton(id, request)
                    if (button != null) {
                        call.respond(button)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Button not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request"))
                }
            }

            delete("/{id}") {
                try {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing button ID")
                    )
                    val deleted = buttonService.deleteButton(id)
                    if (deleted) {
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "Button not found"))
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid button ID"))
                }
            }
        }
    }
}
