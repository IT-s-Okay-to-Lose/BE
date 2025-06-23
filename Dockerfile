FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY ./build/libs/IOTL-0.0.1-SNAPSHOT.jar app.jar
COPY .env .env
ADD https://repo1.maven.org/maven2/io/sentry/sentry-opentelemetry-agent/8.14.0/sentry-opentelemetry-agent-8.14.0.jar /app/sentry-agent.jar

# ENTRYPOINT 수정: Sentry Agent 적용
ENTRYPOINT ["sh", "-c", "SENTRY_AUTO_INIT=false java -javaagent:/app/sentry-agent.jar -jar app.jar"]
