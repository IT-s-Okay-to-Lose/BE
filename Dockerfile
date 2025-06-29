FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY ./build/libs/IOTL-0.0.1-SNAPSHOT.jar app.jar
# ENTRYPOINT 수정: Sentry Agent 적용
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=test"]