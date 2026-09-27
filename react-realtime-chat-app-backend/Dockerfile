# =======================================================
# Stage 1: Build the application using Maven
# =======================================================
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src ./src

RUN ./mvnw clean package -DskipTests

# =======================================================
# Stage 2: Create the final lightweight runtime image
# =======================================================
FROM eclipse-temurin:17.0.13_11-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]