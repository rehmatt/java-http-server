# java-http-server

This project provides a built-in http server, 
which can handle GET and HEAD requests to a user specified 
file system directory under the API /root/*.
Besides that the http server provides a simple Wall application to 
post comments and display them. Therefore extra endpoints were implemented, 
which uses a MongoDB as database.

## Implementation

The implementation is done with Java 11 and the project uses maven to build the artifacts.

### ServerApplication

The main method is located in the class ServerApplication. It allows to pass an additional command line argument to specify a different root directory than the default one "./".

All other used classes are implemented as Singleton and will be initialized before starting the http server and after the application.properties were read.

The application.properties can be overridden by specifying the exact same VM args

Default values:
* server.host=localhost
* server.port=8080
* mongodb.host=localhost
* mongodb.port=27017
* mongodb.database=wallApp  

### HttpServer

The http server is implemented with the package java.nio.channels, 
which provides a non-blocking server solution. The server is started on localhost:8080 by default
and listens to incoming requests. After the request is accepted the requests is read.
The request is handled within two specific controller classes. 
The response is written back to the client socket channel. 

### FileController

The FileController class handles requests to the local file system and serves the static content for the Wall-Application. 
Therefore the java.nio.file.* package is used intensively.

For creating the JSON string for listing all files and subdirectory of a requested directory the third party library org.json is used.  

### CommentController

To handle GET and POST requests from the Wall-Application the CommentController uses the CommentCollection class.

### HttpController (abstract)

The abstract HttpController provides methods for creating responses and is used by both mentioned controller classes as parent class.

### HttpStatus

The enum HttpStatus provide some Http status codes and their responses.
* 200 OK
* 401 Access Denied
* 404 Not Found
* 500 Internal Server Error

### MongoDBClient

Here the connection to the MongoDB is configured and established.

### CommentCollection

The CommentCollection provides two methods. One for persisting Comments to the MongoDB and one for retrieving the saved comments.  

### Wall-Application

The frontend is implemented with an Angular 8 application and is served as static content from inside the Java application. 

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

### Logging

The third party library logback is used for logging.

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
