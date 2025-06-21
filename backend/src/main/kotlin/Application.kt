package com.example

import io.ktor.server.application.*
import org.flywaydb.core.Flyway

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    migrateDatabase()

    configureRouting()
}

fun migrateDatabase() {
    Flyway.configure()
        .dataSource(Env.jdbcUrl, Env.dbUser, Env.dbPassword)
        .locations("classpath:db/migration")
        .load()
        .migrate()
}