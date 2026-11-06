
# Stage 1: Build the Java application
# Use a Maven image to build the application and its dependencies
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory inside the container,
# the workdir is where the commands will be run and files will be copied to

WORKDIR /build

# Copy the pom.xml and source code to the container
COPY pom.xml .
COPY src ./src

# Build the application and copy the dependencies to a separate directory
RUN mvn -q -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/dependency

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/dependency ./libs

EXPOSE 8081

CMD ["java", "-cp", "/app/classes:/app/libs/*", "com.bookstore.App"]
