FROM openjdk:17-slim-buster AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src

RUN ./gradlew clean installDist --no-daemon

FROM openjdk:17-slim-buster

WORKDIR /app

# Copy the built application from the build stage
COPY --from=build /app/build/install/buttontrack .
COPY --from=build /app/src/main/resources/application.conf ./conf/

EXPOSE 8080

CMD ["bin/buttontrack", "-config", "conf/application.conf"]
