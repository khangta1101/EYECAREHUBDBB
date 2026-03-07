# Stage 1: Build file JAR
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Build bỏ qua test để deploy nhanh
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Cổng mặc định của Spring Boot
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
