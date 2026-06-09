FROM eclipse-temurin:21-jre

WORKDIR /app

ARG BUILD_VERSION
COPY stable/target/stable-${BUILD_VERSION}.jar stable.jar

EXPOSE 8085

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-jar", "stable.jar"]