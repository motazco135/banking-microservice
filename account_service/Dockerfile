# ──────────────────────────────────────────────────────────────────────────────
# Stage 1 — Build
# Uses Maven wrapper cache layer so dependencies are not re-downloaded on every
# source change (only re-downloaded when pom.xml changes).
# ──────────────────────────────────────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Cache dependency layer — only invalidated when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source and build, skipping tests (tests run in CI)
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress

# Extract Spring Boot layered JAR for optimal image caching
RUN java -Djarmode=layertools \
         -jar target/account_service-*.jar \
         extract --destination target/extracted

# ──────────────────────────────────────────────────────────────────────────────
# Stage 2 — Runtime
# Minimal JRE image; layers ordered from least to most frequently changed
# so Docker cache is reused on code-only changes.
# ──────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

# Spring Boot layered JAR — copy in dependency-change frequency order
COPY --from=build /workspace/target/extracted/dependencies          ./
COPY --from=build /workspace/target/extracted/spring-boot-loader   ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies ./
COPY --from=build /workspace/target/extracted/application          ./

EXPOSE 8082

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
