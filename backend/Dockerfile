# syntax=docker/dockerfile:1


# 1. Estágio de dependências (baixa as libs usando cache do Docker)
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /build

COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
COPY pom.xml pom.xml

RUN --mount=type=cache,target=/root/.m2 ./mvnw dependency:go-offline -B -DskipTests


COPY ./src src/
RUN  --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests


# 2. Estágio final (apenas o JRE leve, rodando com usuário seguro não-root)
FROM eclipse-temurin:21-jre-jammy AS final
WORKDIR /app

# Cria usuário não-root por segurança
ARG UID=10001
RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser
USER appuser

# Copia as camadas separadas para aproveitar o cache do Docker
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# Launcher oficial do Spring Boot 3.2+
ENTRYPOINT [ "java", "-jar", "app.jar" ]