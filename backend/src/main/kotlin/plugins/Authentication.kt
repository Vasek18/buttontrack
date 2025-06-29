package com.buttontrack.plugins

import com.buttontrack.service.AuthService
import com.buttontrack.service.UserInfo
import com.buttontrack.service.UserSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.util.*

class AuthenticationPlugin {
    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Unit, AuthenticationPlugin> {
        override val key = AttributeKey<AuthenticationPlugin>("Authentication")
        
        override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit): AuthenticationPlugin {
            val plugin = AuthenticationPlugin()
            
            pipeline.intercept(ApplicationCallPipeline.Call) {
                // Skip authentication for the auth endpoint
                if (call.request.path() == "/api/auth" || call.request.path() == "/api/logout") {
                    return@intercept
                }
                
                // Get session from cookie
                val session = call.sessions.get<UserSession>()
                if (session == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "No valid session"))
                    return@intercept finish()
                }
                
                // Convert session to UserInfo (no DB lookup needed)
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

val UserInfoKey = AttributeKey<UserInfo>("UserInfo")

fun ApplicationCall.getUserInfo(): UserInfo? = attributes.getOrNull(UserInfoKey)