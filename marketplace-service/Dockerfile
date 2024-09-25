# Stage 1: Maven build
FROM maven:3.8.1-openjdk-17 AS build-api
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

# Stage 2: Run the application
FROM tomcat:10.1-jdk17-openjdk-slim
WORKDIR /app
# Remove the default ROOT webapp to avoid conflicts
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Copy the WAR file to the webapps directory in Tomcat
ARG BUILD_VERSION
COPY --from=build-api /app/target/marketplace-service-${BUILD_VERSION}.war /usr/local/tomcat/webapps/marketplace-service.war

# Expose port 8080
EXPOSE 8080

# Change ownership of the shared volume
RUN mkdir -p /app/data/market-cache
RUN chown -R root:root /app/data/market-cache

USER root

# Start Tomcat
CMD ["catalina.sh", "run"]

