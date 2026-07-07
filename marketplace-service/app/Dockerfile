FROM eclipse-temurin:25-jre

WORKDIR /app

ARG BUILD_VERSION
COPY app/target/app-${BUILD_VERSION}.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+ExitOnOutOfMemoryError", "-jar", "app.jar"]