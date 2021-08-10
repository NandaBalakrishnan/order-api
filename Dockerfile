FROM openjdk:8-jdk-alpine

MAINTAINER nandathoomathy@gmail.com

ADD build/libs/orderapi-0.0.1-SNAPSHOT.jar /

EXPOSE 8082

ENTRYPOINT ["java","-jar", "orderapi-0.0.1-SNAPSHOT.jar"]
