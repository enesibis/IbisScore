FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/ibisscore-data-ingestion-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
