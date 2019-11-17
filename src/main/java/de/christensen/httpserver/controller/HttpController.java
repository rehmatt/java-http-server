package de.christensen.httpserver.controller;

import de.christensen.httpserver.server.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class HttpController {

    static final String CONTENT_TYPE_APPLICATION_JSON = "application/json; charset=utf-8";
    private static final Logger LOG = LoggerFactory.getLogger(HttpController.class);
    private static final String NEW_LINE = "\n";
    private static final String DATE = "Date: ";
    private static final String SERVER_OS = "Server: " + System.getProperty("os.name");
    private static final String LAST_MODIFIED = "Last-Modified: ";
    private static final String CONTENT_LENGTH = "Content-Length: ";
    private static final String CONTENT_TYPE = "Content-Type: ";
    private static final String RESPONSE_CONNECTION_CLOSED = "Connection: Closed\n\n";

    public static String createErrorResponse(HttpStatus httpStatus) {
        return httpStatus.getResponseText() + NEW_LINE +
                DATE + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME) + NEW_LINE +
                SERVER_OS + NEW_LINE +
                RESPONSE_CONNECTION_CLOSED;
    }

    String createResponse(HttpStatus httpStatus, String contentType, String content, String lastModified) {
        //build response string by calling head response method and adding content at the end
        return createHeadResponse(httpStatus, contentType, content, lastModified) +
                content;
    }

    String createHeadResponse(HttpStatus httpStatus, String contentType, String content, String lastModified) {
        //build head response by calling basic response method and adding content information
        final String headResponse = getBasicResponse(httpStatus) +
                LAST_MODIFIED + lastModified + NEW_LINE +
                CONTENT_TYPE + contentType + NEW_LINE +
                //calling getBytes() because of e.g. German Umlauts
                CONTENT_LENGTH + content.getBytes().length + NEW_LINE +
                RESPONSE_CONNECTION_CLOSED;
        LOG.debug("Response head: {}", headResponse);
        return headResponse;
    }

    private String getBasicResponse(HttpStatus httpStatus) {
        return httpStatus.getResponseText() + NEW_LINE +
                DATE + ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME) + NEW_LINE +
                SERVER_OS + NEW_LINE;
    }

    public static String getRequestUri(String request, int leadingChars) {
        //remove leading chars and pending " HTTP/"
        final String uri = request.substring(leadingChars, request.indexOf(" HTTP/")).trim();
        final String decodedUri = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        if (decodedUri.endsWith("/")) {
            return decodedUri.substring(0, decodedUri.length() - 1);
        } else {
            return decodedUri;
        }
    }

}
