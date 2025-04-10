FROM eclipse-temurin:17
WORKDIR /app
COPY target/ship-proxy-1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
