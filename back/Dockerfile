FROM gradle:8.10-jdk17 AS build
USER gradle
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle build -x test --no-daemon --stacktrace

FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
