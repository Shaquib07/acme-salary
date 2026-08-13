FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY src src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /app/data
COPY --from=build /src/target/salary-api-0.1.0.jar app.jar
EXPOSE 8080
ENV ACME_DB_PATH=/app/data/acme-salary.db
ENTRYPOINT ["java", "-jar", "app.jar"]
