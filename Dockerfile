# =====================================================================
# Build multi-stage da API de Fila de Atendimento
# =====================================================================

# ---------- Estagio 1: compilacao ----------
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copia primeiro o POM para aproveitar o cache de dependencias do Docker
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline

# Copia o codigo e empacota o fat jar
COPY src ./src
RUN mvn -B -q -DskipTests package

# ---------- Estagio 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Executa como usuario sem privilegios
RUN addgroup -S app && adduser -S app -G app \
    && chown -R app:app /app

COPY --from=build --chown=app:app /build/target/fila-atendimento.jar /app/app.jar

USER app

EXPOSE 8080

ENV TZ=America/Sao_Paulo
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -Duser.timezone=America/Sao_Paulo"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
