FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY target/ibisscore-user-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
