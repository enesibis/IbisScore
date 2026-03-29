FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/ibisscore-match-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
