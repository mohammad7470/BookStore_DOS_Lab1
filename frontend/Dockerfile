
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/dependency

# Stage 2: Run
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/dependency ./libs

EXPOSE 8080

CMD ["java", "-cp", "/app/classes:/app/libs/*", "com.bookstore.App"]
