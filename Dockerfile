# Stage 1: Build React frontend
FROM node:24-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot JAR (with frontend baked in as static resources)
FROM maven:3.9-eclipse-temurin-21-alpine AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
RUN mvn dependency:go-offline -B
COPY backend/src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN mvn package -DskipTests -B

# Stage 3: Minimal JRE runtime
FROM eclipse-temurin:21.0.11_10-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/thank-you-matcher-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]