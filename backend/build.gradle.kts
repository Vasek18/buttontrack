// backend/build.gradle.kts
plugins {
    // Kotlin JVM plugin version explicitly defined here as it's the plugin itself
    kotlin("jvm") version "2.1.21"
    // Use alias for Ktor plugin now that it's in libs.versions.toml
    alias(libs.plugins.ktor.plugin)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.buttontrack"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

// Removed the repositories block from here.
// Repositories should be defined in settings.gradle.kts when using FAIL_ON_PROJECT_REPOS mode.

dependencies {
    // Ktor core dependencies from libs.versions.toml
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.host.common)

    // Logging dependency from libs.versions.toml
    implementation(libs.logback.classic)

    // Database - Exposed ORM dependencies from libs.versions.toml
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql.driver)
    implementation(libs.hikaricp) // Added HikariCP dependency

    // Database Migrations - Flyway dependencies from libs.versions.toml
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    // Testing dependencies from libs.versions.toml
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlin.reflect) // Required by Exposed
}

// The ext block is no longer needed as versions are managed in libs.versions.toml

// Task to run Flyway migrations (unchanged)
tasks.register("dbMigrate") {
    group = "database"
    description = "Runs Flyway database migrations"
    doLast {
        println("Flyway migration task. Migrations will be applied by the backend application.")
    }
}
