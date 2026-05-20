FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY app/target/*.jar app.jar

EXPOSE 8080 5005

CMD java \
-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
-jar app.jar