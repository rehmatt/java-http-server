# java-http-server

This project provides a built-in http server, 
which can handle GET and HEAD requests to a user specified 
file system directory under the API /root/*.
Besides that the http server provides a simple Wall application to 
post comments and display them. Therefore extra endpoints were implemented, 
which uses a MongoDB as database.

## API

### Serve the file system

* GET /root/**
  * returns the files content, if the url denotes a file
  * returns a json string containing all files and subdirectories, if the url denotes a directory  

* HEAD /root/**
  * returns the same response than the GET request, but without the content (file / json string)

### Wall-Application
  
* GET /*
  * returns the static content to display the Wall-Application (e.g. /index.html)

* GET /api/comments
  * returns all saved comments in the local MongoDB
  
* POST /api/comments/comment
  * stores the passed json object into the local MongoDB

## How to use it

There are three different ways to use the application.

1. Use an IDE e.g. IntelliJ
2. Use the executable Jar under the folder executable
3. Use the provided Dockerfile to build a Docker image

### Use an IDE

#### Pre-requisites

* IDE (Eclipse or IntelliJ)
* Java 11
* Maven
* nodeJS
* npm
* MongoDB running on localhost:27017

#### Steps to run the application

* Clone the repository
* Execute a "mvn clean install"
* Run the application (main method is in de.christensen.httpserver.ServerApplication)
* To specify a different directory use the program arguments to pass in the desired path

### Use the executable Jar under the folder executable

#### Pre-requisites

* Java 11
* MongoDB running on localhost:27017

#### Steps to run the application

* Download the http-server.jar
* Run a terminal window
* Run the application with "java -jar http-server.jar"
* To specify a different directory use the program arguments to pass in the desired path
  * E.g. java -jar http-server.jar /path/to/directory

### Use the provided Dockerfile to build a Docker image

#### Pre-requisites

* IDE (Eclipse or IntelliJ)
* Java 11
* Maven
* nodeJS
* npm
* MongoDB running on localhost:27017
* docker

#### Steps to run the application

* Clone the repository
* Execute "mvn clean install"
* Execute "docker build -t http-server:1.0.0-SNAPSHOT ."
* Execute "docker run -p 8080:8080 -e "JAVA_OPTS=-Dmongodb.host=host.docker.internal" http-server"
* To specify a different directory add JAVA_ARGS=/path/to/directory to docker run
  * E.g. "docker run -p 8080:8080 -e "JAVA_OPTS=-Dmongodb.host=host.docker.internal" -e "JAVA_ARGS=/path/to/directory" http-server"
  * Important: Path has to be inside the docker container unless you mount a host directory during start up with "-v"
