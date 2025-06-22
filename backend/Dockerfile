# backend/Dockerfile
# Use a slim Java base image for production
FROM openjdk:17-slim-buster as build

# Set working directory
WORKDIR /app

# Copy the Gradle wrapper and project files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

# Build the application
# Use --no-daemon for CI/CD environments
RUN ./gradlew clean installDist --no-daemon

# Runtime image
FROM openjdk:17-slim-buster

WORKDIR /app

# Copy the built application from the build stage
COPY --from=build /app/build/install/buttontrack .

# Expose the port Ktor listens on
EXPOSE 8080

# Command to run the application
# buttontrack is derived from rootProject.name in settings.gradle.kts
# and the application block in build.gradle.kts
CMD ["bin/buttontrack"]
