# Multi-stage Dockerfile for RippleReady Backend (Root Repository Context)
# Compatible with Render, Railway, Fly.io, and Docker Compose

# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY backend/pom.xml .
COPY backend/src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S ripplegroup && adduser -S rippleuser -G ripplegroup
RUN mkdir -p /app/data && chown -R rippleuser:ripplegroup /app

USER rippleuser:ripplegroup

COPY --from=builder --chown=rippleuser:ripplegroup /build/target/*.jar app.jar

ENV PORT=8080
ENV SQLITE_DB_PATH=data/rippleready.db
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -Xmx384m -Xss512k -XX:+UseG1GC -jar app.jar"]
