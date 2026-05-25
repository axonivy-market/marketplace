FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG BUILD_VERSION
COPY stable/target/stable-${BUILD_VERSION}.jar stable.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-jar", "stable.jar"]