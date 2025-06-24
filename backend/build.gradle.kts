plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor.plugin)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.buttontrack"
version = "0.0.1"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    // Ktor core dependencies from libs.versions.toml
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.cors)

    // Logging dependency from libs.versions.toml
    implementation(libs.logback.classic)

    // Database - Exposed ORM dependencies from libs.versions.toml
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javatime)
    implementation(libs.postgresql.driver)
    implementation(libs.hikaricp) // Added HikariCP dependency

    // Database Migrations - Flyway dependencies from libs.versions.toml
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    // Testing dependencies from libs.versions.toml
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotlin.reflect) // Required by Exposed
    testImplementation("com.h2database:h2:2.2.224") // In-memory database for testing
}

tasks.named<JavaExec>("run") {
    // Load environment variables from root .env file
    val envFile = file("../.env")
    if (envFile.exists()) {
        envFile.readLines().forEach { line ->
            if (line.isNotBlank() && !line.startsWith("#") && line.contains("=")) {
                val (key, value) = line.split("=", limit = 2)
                val trimmedKey = key.trim()
                var trimmedValue = value.trim()
                
                // Override DB_URL for local development
                if (trimmedKey == "DB_URL") {
                    trimmedValue = trimmedValue.replace("://db:", "://localhost:")
                }
                
                environment(trimmedKey, trimmedValue)
                systemProperty(trimmedKey, trimmedValue)
            }
        }
    }
}
