FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY build/libs/sales-service-0.0.1-SNAPSHOT.jar sales-service.jar

EXPOSE 5001

ENTRYPOINT ["java","-jar","sales-service.jar"]


