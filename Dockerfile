# Etapa de build (Gradle)
FROM gradle:latest-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# Runtime (JRE)
FROM eclipse-temurin:21-jre
WORKDIR /app

# Render expone un PORT -> tu app debe escuchar en ese puerto
ENV PORT=8080
EXPOSE 8080

# Copia el JAR construido
COPY --from=build /app/build/libs/*.jar app.jar

# Forzamos que Spring use el puerto de Render
ENTRYPOINT ["java","-Dserver.port=${PORT}","-jar","/app.jar"]
