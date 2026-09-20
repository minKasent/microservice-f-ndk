# syntax=docker/dockerfile:1
# ========================================
# Stage 1: Build (discovery-server)
# ========================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 1. Cache discovery-server dependencies
COPY discovery-server/pom.xml ./discovery-server/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f discovery-server/pom.xml dependency:go-offline -B || true

# 2. Copy discovery-server source and build package
COPY discovery-server/src ./discovery-server/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f discovery-server/pom.xml clean package -DskipTests

# 3. Extract layers using Spring Boot 4 'tools' jarmode
RUN java -Djarmode=tools -jar discovery-server/target/*.jar extract --layers --launcher --destination extracted


# ========================================
# Stage 2: Runtime
# ========================================
FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy layers in order (dependencies first for better caching)
COPY --chown=spring:spring --from=builder /build/extracted/dependencies/ ./
COPY --chown=spring:spring --from=builder /build/extracted/spring-boot-loader/ ./
COPY --chown=spring:spring --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --chown=spring:spring --from=builder /build/extracted/application/ ./

# Copy OpenTelemetry Java Agent from shared infra/otel
COPY --chown=spring:spring infra/otel/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar

# Switch to non-root user
USER spring:spring

EXPOSE 8761

# Default OpenTelemetry settings for Docker network
ENV OTEL_SERVICE_NAME=discovery-server \
    OTEL_EXPORTER_OTLP_ENDPOINT=http://tempo:4317 \
    OTEL_EXPORTER_OTLP_PROTOCOL=grpc \
    OTEL_TRACES_EXPORTER=otlp \
    OTEL_METRICS_EXPORTER=none \
    OTEL_LOGS_EXPORTER=none \
    OTEL_PROPAGATORS=tracecontext,baggage

# Enable OpenTelemetry agent via JVM standard environment variable
ENV JAVA_TOOL_OPTIONS="-javaagent:/app/opentelemetry-javaagent.jar"

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]
