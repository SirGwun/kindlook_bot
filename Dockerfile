# Стадия сборки
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Копируем остальной код и собираем
COPY . .
RUN mvn clean package -DskipTests

# Финальный образ только с JRE
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/kindlookBot-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]