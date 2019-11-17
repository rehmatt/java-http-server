FROM openjdk:11

MAINTAINER Mattis Christensen

ARG JAVA_OPTS=""
ARG JAVA_ARGS=""

#add and use user
RUN useradd --create-home -s /bin/bash app
WORKDIR /usr/app
USER app

#configure application
ADD /target/http-server-*-jar-with-dependencies.jar /usr/app/http-server.jar
ENTRYPOINT exec java $JAVA_OPTS  -jar http-server.jar $JAVA_ARGS

EXPOSE 8080