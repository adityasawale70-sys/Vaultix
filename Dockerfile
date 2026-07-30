# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:21-jdk-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and pom.xml
COPY backend/vaultix-backend/mvnw .
COPY backend/vaultix-backend/.m2 .m2
COPY backend/vaultix-backend/pom.xml .

# Download dependencies (this step is cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY backend/vaultix-backend/src ./src

# Package the application (skip tests for faster build in CI, but you can remove -DskipTests if you want to run tests)
RUN ./mvnw package -DskipTests

# Use a smaller runtime image for the final stage
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app
WORKDIR /app

# Copy the jar built image
COPY --from=build /app/target/vaultix-backend-1.0.0.jar ./app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","/app/app.jar"]