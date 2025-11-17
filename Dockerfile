FROM maven:3.9.8-eclipse-temurin-21 as build
WORKDIR /project

COPY pom.xml .
RUN mvn -ntp dependency:resolve

COPY src ./src
RUN mvn -ntp package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /project/target/quarkus-app/ ./

CMD ["java", "-jar", "quarkus-run.jar"]
