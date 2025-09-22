FROM gradle:8.10.2-jdk23 AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
RUN gradle build --no-daemon || return 0
COPY . .
RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:23-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*-all.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]