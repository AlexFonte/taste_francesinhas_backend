# == Build stage ==============================================================
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app

# Copia wrapper y pom primero para aprovechar la cache de capas de Docker
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copia el código y compila (sin tests, se ejecutan en CI)
COPY src ./src
RUN ./mvnw package -DskipTests -q

# == Runtime stage =============================================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} $JAVA_OPTS -jar app.jar"]