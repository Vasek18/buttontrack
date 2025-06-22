package com.buttontrack

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Configure Ktor features
    configureSerialization()
    configureRouting()

    // Initialize database connection and run migrations
    configureDatabase()
    
    // Log that the application has started
    log.info("Button Track backend started")
}
