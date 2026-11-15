# syntax=docker/dockerfile:1

# ---------- Etapa 1: compilación ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Descargar dependencias primero para aprovechar la caché de capas
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Los tests de contexto necesitan las claves, por eso se omiten en la imagen
RUN mvn -B -q -DskipTests package

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Usuario sin privilegios
RUN groupadd --system --gid 10001 app \
    && useradd --system --uid 10001 --gid app --no-create-home app \
    && mkdir -p /app/data /app/logs /app/claves \
    && chown -R app:app /app

COPY --from=build /build/target/*.jar /app/app.jar

# Rutas dentro del contenedor. Las claves se montan como volumen de solo lectura.
# La consola H2 se desactiva fuera del entorno de desarrollo.
ENV RUTA_CLAVE_MAESTRA=/app/claves/tink_keyset.json \
    RUTA_CLAVE_JWT=/app/claves/jwt_secret.key \
    RUTA_LOG_SEGURIDAD=/app/logs/log_seguridad.txt \
    SPRING_H2_CONSOLE_ENABLED=false

USER app
EXPOSE 8081

# Comprueba que la aplicación acepta conexiones (la imagen no incluye curl)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD bash -c 'exec 3<>/dev/tcp/127.0.0.1/8081' || exit 1
VOLUME ["/app/data", "/app/logs"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
