package de.christensen.httpserver.server;

public enum HttpStatus {

    OK(200),
    CREATED(201){
        @Override
        public String getResponseText() {
            return "HTTP/1.1 201 Created";
        }
    },
    ACCESS_DENIED(401){
        @Override
        public String getResponseText() {
            return "HTTP/1.1 401 Access Denied";
        }
    },
    NOT_FOUND(404){
        @Override
        public String getResponseText() {
            return "HTTP/1.1 404 Not Found";
        }
    },
    INTERNAL_SERVER_ERROR(500){

        public String getResponseText() {
            return "HTTP/1.1 500 Internal Server Error";
        }
    };

    private final int code;

    HttpStatus(int code) {
        this.code = code;
    }

    public String getResponseText() {
        return "HTTP/1.1 200 OK";
    }

    public int getCode() {
        return code;
    }
}
