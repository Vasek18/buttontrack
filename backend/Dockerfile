# backend/Dockerfile
FROM gradle:8.4-jdk17 AS builder
WORKDIR /app
COPY . .
RUN ./gradlew installDist

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=builder /app/build/install/* /app/
EXPOSE 8080
CMD ["bin/backend"]