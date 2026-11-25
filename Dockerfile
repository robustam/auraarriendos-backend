# ---------- Etapa 1: compilar el proyecto con Maven ----------
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Primero solo el pom: así Docker reutiliza las dependencias descargadas si no cambian
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- Etapa 2: imagen liviana solo con Java para ejecutar ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Usuario sin privilegios (buena práctica de seguridad)
RUN addgroup -S aura && adduser -S aura -G aura
COPY --from=build /app/target/auraarriendos-backend.jar app.jar
USER aura
ENV SPRING_PROFILES_ACTIVE=docker \
    PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
