package com.buttontrack.plugins

import com.buttontrack.service.AuthService
import com.buttontrack.service.UserInfo
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.util.*

class AuthenticationPlugin {
    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Unit, AuthenticationPlugin> {
        override val key = AttributeKey<AuthenticationPlugin>("Authentication")
        
        override fun install(pipeline: ApplicationCallPipeline, configure: Unit.() -> Unit): AuthenticationPlugin {
            val plugin = AuthenticationPlugin()
            
            pipeline.intercept(ApplicationCallPipeline.Call) {
                val authHeader = call.request.headers["Authorization"]
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Missing or invalid authorization header"))
                    return@intercept finish()
                }
                
                val token = authHeader.substring(7)
                val authService = AuthService()
                val userInfo = authService.verifyToken(token)
                
                if (userInfo == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@intercept finish()
                }
                
                call.attributes.put(UserInfoKey, userInfo)
            }
            
            return plugin
        }
    }
}

val UserInfoKey = AttributeKey<UserInfo>("UserInfo")

fun ApplicationCall.getUserInfo(): UserInfo? = attributes.getOrNull(UserInfoKey)