FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /project

COPY pom.xml .
RUN mvn -ntp dependency:resolve

COPY src ./src
RUN mvn -ntp package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /project/target/*-runner.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/app.jar"]
