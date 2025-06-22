package com.buttontrack

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Configure Ktor features
    configureSerialization()
    configureRouting()

    // Initialize database connection and run migrations
    configureDatabase()
}
