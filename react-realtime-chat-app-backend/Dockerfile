# =======================================================
# Stage 1: Build the application using Maven
# =======================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build

# Set the working directory inside the build container
WORKDIR /build

# Copy the pom.xml and the maven wrapper configurations first
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies (this layer is cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline

# Copy the actual source code
COPY src ./src

# Compile and package the application
RUN ./mvnw clean package -DskipTests

# =======================================================
# Stage 2: Create the final lightweight runtime image
# =======================================================
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy ONLY the compiled JAR file from the 'build' stage
COPY --from=build /build/target/*.jar app.jar

# Expose the application port
EXPOSE 8088

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]