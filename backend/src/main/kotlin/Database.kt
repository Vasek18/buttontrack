package com.buttontrack

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object DatabaseFactory {
    fun init(environment: ApplicationEnvironment) {
        val dbUrl = System.getenv("DB_URL") ?: System.getProperty("DB_URL") ?: environment.config.propertyOrNull("ktor.deployment.dbUrl")?.getString()
            ?: throw IllegalStateException("DB_URL environment variable or system property is required")
        val dbUser = System.getenv("DB_USER") ?: System.getProperty("DB_USER") ?: environment.config.propertyOrNull("ktor.deployment.dbUser")?.getString()
            ?: throw IllegalStateException("DB_USER environment variable or system property is required")
        val dbPassword = System.getenv("DB_PASSWORD") ?: System.getProperty("DB_PASSWORD") ?: environment.config.propertyOrNull("ktor.deployment.dbPassword")?.getString()
            ?: throw IllegalStateException("DB_PASSWORD environment variable or system property is required")

        val config = HikariConfig().apply {
            jdbcUrl = dbUrl
            username = dbUser
            password = dbPassword
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(config)

        try {
            val flyway = Flyway.configure().dataSource(dataSource).load()
            flyway.migrate()
        } catch (e: Exception) {
            environment.log.error("Failed to run Flyway migrations: ${e.message}")
        }

        Database.connect(dataSource)
        environment.log.info("Database connected successfully")
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}

fun Application.configureDatabase() {
    try {
        DatabaseFactory.init(environment)
    } catch (e: Exception) {
        log.error("Failed to initialize database: ${e.message}")
    }
}
