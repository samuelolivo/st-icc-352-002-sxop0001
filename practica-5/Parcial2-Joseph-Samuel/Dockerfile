# Etapa 1: Construcción (Build)
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

RUN ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew build -x test --no-daemon

# Etapa 2: Producción (Runtime)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar ./app.jar

EXPOSE 7070

CMD ["java", "-jar", "app.jar"]