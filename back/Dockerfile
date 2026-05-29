FROM gradle:8.10-jdk17 AS build
USER gradle
WORKDIR /home/gradle/project

# 의존성 레이어 분리: build.gradle이 바뀌지 않으면 이 레이어 캐시 재사용
COPY --chown=gradle:gradle build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY --chown=gradle:gradle src ./src
RUN gradle build -x test --no-daemon

# JRE-alpine: JDK 대비 이미지 크기 약 300MB 절감
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
