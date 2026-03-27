# Stage 1: Build
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Install Maven manual
RUN apk add --no-cache maven

# Copy pom.xml untuk caching dependency
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code dan build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
# Menggunakan Alpine agar image sangat ringan
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy hasil build jar
COPY --from=build /app/target/*.jar app.jar

# Expose port aplikasi
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]