# Multi-stage build for the Spring Boot backend
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew \
    && (./gradlew spotlessCheck --no-daemon || (./gradlew spotlessApply --no-daemon && ./gradlew spotlessCheck --no-daemon)) \
    && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
