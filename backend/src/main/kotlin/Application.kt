package com.buttontrack

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Initialize database connection and run migrations first
    configureDatabase()
    
    // Configure Ktor features
    configureSerialization()
    configureRouting()
}
