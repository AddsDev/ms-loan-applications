# ---- BUILD ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace

COPY gradlew gradle/ ./
COPY gradle/wrapper ./gradle/wrapper
COPY settings.gradle build.gradle main.gradle ./
COPY gradle.properties ./gradle.properties

COPY applications ./applications
COPY domain ./domain
COPY infrastructure ./infrastructure

RUN chmod +x ./gradlew

RUN ./gradlew clean --no-daemon  :app-service:bootJar -x test

# ---- RUNTIME ----
# -jammy
FROM eclipse-temurin:17-jre

# Docker Profile
ENV SPRING_PROFILES_ACTIVE=docker \
    SERVER_PORT=8080 \
    DB_HOST=postgres-db \
    DB_PORT=5433 \
    DB_NAME=credi_ya_db_docker \
    DB_SCHEMA=ms_applications \
    DB_USER=postgres_docker \
    DB_PASSWORD=postgres_docker \
    AUTH_JWKS_URI=http://auth-service:8080 \
    AUTH_ISSUER_URI=http://auth-service:8080 \
    SQS_REQUEST_QUEUE=https://sqs.us-east-1.amazonaws.com/00000000/laon_request_queue \
    SQS_AUTOMATIC_QUEUE=https://sqs.us-east-1.amazonaws.com/00000000/loan_automatic_queue \
    SQS_REPORT_QUEUE=https://sqs.us-east-1.amazonaws.com/00000000/loan_reports_queue \
    SQS_RESULT_AUTOMATIC_QUEUE=https://sqs.us-east-1.amazonaws.com/00000000/result_automatic_queue \
    CORS_ALLOWED_ORIGINS=*

# Usuario no root
RUN useradd -ms /bin/bash spring
USER spring
WORKDIR /home/spring

COPY --from=build /workspace/applications/app-service/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/home/spring/app.jar"]
