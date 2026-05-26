# Build stage
FROM eclipse-temurin:23-jdk AS build
WORKDIR /app

# Copy the maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Make the wrapper executable (in case it lost its permissions)
RUN chmod +x mvnw

# Download dependencies (this caches them in a separate docker layer)
RUN ./mvnw dependency:go-offline

# Copy the source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:23-jre
WORKDIR /app

# Render passes the PORT environment variable, defaulting to 8080
ENV PORT=8080
EXPOSE 8080

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]
