FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY ./build/libs/IOTL-0.0.1-SNAPSHOT.jar app.jar
COPY .env .env
# ENTRYPOINT 수정: Sentry Agent 적용
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
