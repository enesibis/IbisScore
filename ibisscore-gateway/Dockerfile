FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/ibisscore-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
