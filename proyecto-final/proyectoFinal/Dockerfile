# ETAPA 1: Build
FROM eclipse-temurin:25-jdk-alpine AS builder

RUN apk add --no-cache gcompat libstdc++

WORKDIR /app
COPY . .
RUN chmod +x gradlew
RUN ./gradlew jar --no-daemon

# ETAPA 2: Run
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 7070
CMD ["java", "-jar", "app.jar"]