FROM maven:3-jdk-11-openj9
MAINTAINER Melvin Chelli <melvin.chelli@dfki.de>

LABEL Description="This image provides a medium server instance for the stigLD demo"

COPY ./ /home/medium-server
RUN cd /home/medium-server \
    && mvn clean install 

EXPOSE 8080

WORKDIR /home/medium-server/target/
ENTRYPOINT ["java","-jar","StigLD-0.0.1-SNAPSHOT.jar"]

