# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -ntp -q dependency:go-offline
COPY src ./src
RUN mvn -B -ntp -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/target/api-gateway-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
