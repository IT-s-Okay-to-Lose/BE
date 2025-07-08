FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY ./build/libs/IOTL-0.0.1-SNAPSHOT.jar app.jar
# tzdata 설치 및 시간대 파일 설정
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone \
    ENV TZ=Asia/Seoul \
COPY .env .env
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
