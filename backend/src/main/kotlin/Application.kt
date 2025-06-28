package com.buttontrack

import com.buttontrack.service.UserSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.sessions.*
import kotlin.time.Duration.Companion.days

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Initialize database connection and run migrations first
    configureDatabase()
    
    // Configure CORS
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowCredentials = true
        allowSameOrigin = true
        allowHost("localhost:3000")
        allowHost("127.0.0.1:3000")
    }
    
    // Configure sessions
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 7.days.inWholeSeconds
            cookie.httpOnly = true
            cookie.secure = false // Set to true in production with HTTPS
            cookie.sameSite = SameSite.Strict
        }
    }
    
    // Configure Ktor features
    configureSerialization()
    configureRouting()
}
