FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace/app

COPY . .
RUN mvn clean package -DskipTests

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:21-jre-jammy
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java", "-cp", "/app:/app/lib/*", "org.example.projectrealizationservice.ProjectRealizationServiceApplication"]
