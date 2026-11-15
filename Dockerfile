# syntax=docker/dockerfile:1

# --- Stage 1: Build executable JAR ---
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

# Cache Maven dependencies before copying application source
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw package -DskipTests -B

# --- Stage 2: Minimal multi-arch JRE runtime ---
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder --chown=appuser:appgroup /workspace/target/*.jar /app/app.jar

USER appuser
EXPOSE 8080

# context-path is /api, so actuator health lives at /api/actuator/health
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/actuator/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:--XX:MaxRAMPercentage=75.0 -XX:+UseG1GC} -jar /app/app.jar"]
