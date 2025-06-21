plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    application
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)

    // Flyway for migrations
    implementation("org.flywaydb:flyway-core:10.13.0")

    // Exposed ORM
    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")

    // PostgreSQL driver
    implementation("org.postgresql:postgresql:42.7.3")

    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}
