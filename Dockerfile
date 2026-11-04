# Construction du jar avec Maven et Java 25
FROM maven:3.9-eclipse-temurin-25 AS construction
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
RUN mvn -q package -DskipTests

# Image finale : uniquement Java et le jar
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=construction /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
