FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/ibisscore-betting-service-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
