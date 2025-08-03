
# Stage 1: Build the application
FROM gradle:8.5.0-jdk17-alpine AS build
WORKDIR /app
COPY build.gradle settings.gradle /app/
COPY src /app/src
RUN gradle bootJar --no-daemon

# Stage 2: Create the final, smaller image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

