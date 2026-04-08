# ---- build stage ----
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
# cache dependencies as a separate layer
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress
# compile and package
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress

# ---- runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/api-gateway-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
