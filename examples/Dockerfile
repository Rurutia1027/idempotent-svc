# Start from a lightweight Java runtime image
FROM eclipse-temurin:17-jre-jammy

# Copy the built jar file from target directory
COPY target/*.jar app.jar

# Expose the port your app runs on (e.g., 19988)
EXPOSE 19988

# Run the jar
ENTRYPOINT ["java", "-jar", "/app.jar"]