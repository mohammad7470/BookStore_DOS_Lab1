
# Stage 1: Build the Java application
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/dependency

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/dependency ./libs

EXPOSE 8082

CMD ["java", "-cp", "/app/classes:/app/libs/*", "com.bookstore.App"]
