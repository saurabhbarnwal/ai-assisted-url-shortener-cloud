FROM gradle:8.10.2-jdk21-alpine AS build
WORKDIR /workspace
COPY settings.gradle build.gradle ./
COPY src ./src
RUN gradle clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/build/libs/*.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
