# ─── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper first (layer-caches dependency downloads if pom.xml unchanged)
COPY Backend/vaultix-backend/mvnw .
COPY Backend/vaultix-backend/.mvn .mvn
COPY Backend/vaultix-backend/pom.xml .

# Pre-fetch all dependencies (cached layer — only re-runs when pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build
COPY Backend/vaultix-backend/src ./src
RUN ./mvnw package -DskipTests

# ─── Stage 2: Runtime ──────────────────────────────────────────────────────────
# Use a slim JRE-only image — no compiler in production
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the compiled jar from the build stage
COPY --from=build /app/target/vaultix-backend-1.0.0.jar ./app.jar

# Run as non-root for security
RUN addgroup -S vaultix && adduser -S vaultix -G vaultix
USER vaultix

EXPOSE 8080

# Pass secrets via environment variables at runtime:
# docker run -e DB_URL=... -e DB_PASSWORD=... -e JWT_SECRET=... vaultix
ENTRYPOINT ["java", "-jar", "/app/app.jar"]