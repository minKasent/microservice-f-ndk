# syntax=docker/dockerfile:1
# ========================================
# Stage 1: Build (common-lib + purchase-service)
# ========================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

# 1. Build and install common-lib to local repo
COPY common-lib ./common-lib
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f common-lib/pom.xml clean install -DskipTests

# 2. Cache purchase-service dependencies
COPY purchase-service/pom.xml ./purchase-service/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f purchase-service/pom.xml dependency:go-offline -B || true

# 3. Copy purchase-service source and build package
COPY purchase-service/src ./purchase-service/src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f purchase-service/pom.xml clean package -DskipTests

# 4. Extract layers using Spring Boot 4 'tools' jarmode
RUN java -Djarmode=tools -jar purchase-service/target/*.jar extract --layers --launcher --destination extracted


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

EXPOSE 8006

# Default OpenTelemetry settings for Docker network
ENV OTEL_SERVICE_NAME=purchase-service \
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
