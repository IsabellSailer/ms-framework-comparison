FROM gradle:5.5-jdk11 AS builder
WORKDIR /home/gradle
COPY build.gradle gradle.properties settings.gradle ./
COPY src ./src/
RUN gradle shadowJar

FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
ENV ARTIFACT_NAME=treatments-all.jar
WORKDIR /usr/treatments/
COPY --from=builder /home/gradle/build/libs/$ARTIFACT_NAME .
EXPOSE 8080
ENTRYPOINT exec java -Dmicronaut.environments=docker -jar $ARTIFACT_NAME