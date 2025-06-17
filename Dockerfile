FROM eclipse-temurin:17-jdk-alpine
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8
COPY ./build/libs/IOTL-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]