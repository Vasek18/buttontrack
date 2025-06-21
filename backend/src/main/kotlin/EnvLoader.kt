package com.example

import io.github.cdimascio.dotenv.Dotenv

object Env {
    private val dotenv = Dotenv.configure()
        .ignoreIfMissing()  // safe if .env not present
        .load()

    val jdbcUrl: String = dotenv["DATABASE_URL"] ?: error("DATABASE_URL not set")
    val dbUser: String = dotenv["DATABASE_USER"] ?: error("DATABASE_USER not set")
    val dbPassword: String = dotenv["DATABASE_PASSWORD"] ?: error("DATABASE_PASSWORD not set")
}
