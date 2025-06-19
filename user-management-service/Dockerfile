FROM eclipse-temurin:21-jdk-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw verify --fail-never

COPY src ./src

RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine AS prod
RUN mkdir /app  \
&& addgroup nonrootgroup; adduser  --ingroup nonrootgroup --disabled-password nonroot
USER nonroot
COPY --from=builder /app/target/*.jar /app/UserManagementService.jar
WORKDIR /app
EXPOSE 8005
ENTRYPOINT ["java","-jar","UserManagementService.jar"]