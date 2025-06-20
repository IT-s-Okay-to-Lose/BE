FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY ./build/libs/IOTL-0.0.1-SNAPSHOT.jar app.jar
COPY .env .env
ENTRYPOINT ["java", "-jar", "app.jar"]