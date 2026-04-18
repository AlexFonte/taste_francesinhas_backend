# ── Build stage ──────────────────────────────────────────────────────────────
FROM maven:3.9-amazoncorretto-25 AS build
WORKDIR /app
COPY . .
RUN mvn -DskipTests clean package

# ── Runtime stage ─────────────────────────────────────────────────────────────
FROM amazoncorretto:25
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} $JAVA_OPTS -jar app.jar"]