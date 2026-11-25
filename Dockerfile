# Fase 1: build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cachea las dependencias: solo se re-descargan si cambia el pom.xml
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B && mv target/*.jar app.jar

# Fase 2: runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/app.jar .

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]