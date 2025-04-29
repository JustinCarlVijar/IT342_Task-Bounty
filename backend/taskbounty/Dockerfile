# Use an official OpenJDK runtime
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the Spring Boot JAR
COPY target/*.jar app.jar

# Expose the port (Cloud Run sets PORT env variable)
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]