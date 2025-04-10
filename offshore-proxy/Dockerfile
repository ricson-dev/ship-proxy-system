FROM eclipse-temurin:17
WORKDIR /app
COPY target/offshore-proxy-1.0.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
