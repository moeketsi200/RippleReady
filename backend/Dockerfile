# Multi-stage Dockerfile for RippleReady Spring Boot Backend
# Optimized for low-RAM cloud hosting (Render, Railway, Koyeb, AWS)

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src

# Install maven and package
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Stage 2: Lightweight Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security compliance (crucial for enterprise/banking standards)
RUN addgroup -S ripplegroup && adduser -S rippleuser -G ripplegroup
USER rippleuser:ripplegroup

COPY --from=builder /build/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

# Keep memory bounded to fit within 512MB free tier containers
ENTRYPOINT ["java", "-Xmx384m", "-Xss512k", "-XX:+UseG1GC", "-jar", "app.jar"]

