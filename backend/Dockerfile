# Multi-stage Dockerfile for RippleReady Spring Boot Backend
# Optimized for cloud deployment on Render, Railway, Fly.io, and AWS

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY pom.xml .
COPY src ./src

# Install maven and build JAR
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Stage 2: Lightweight Alpine JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root user for security compliance
RUN addgroup -S ripplegroup && adduser -S rippleuser -G ripplegroup

# Ensure app directory and SQLite data directory exist with correct permissions
RUN mkdir -p /app/data && chown -R rippleuser:ripplegroup /app

USER rippleuser:ripplegroup

COPY --from=builder --chown=rippleuser:ripplegroup /build/target/*.jar app.jar

ENV PORT=8080
ENV SQLITE_DB_PATH=data/rippleready.db
EXPOSE 8080

# Dynamic port binding from Render ($PORT) with bounded memory for 512MB free tiers
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Xmx384m -Xss512k -XX:+UseG1GC -jar app.jar"]
