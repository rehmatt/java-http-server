package de.christensen.httpserver.controller;

import de.christensen.httpserver.exceptions.HttpHandlerNotInitializedException;
import de.christensen.httpserver.persistence.CommentsCollection;
import de.christensen.httpserver.persistence.entities.Comment;
import de.christensen.httpserver.server.HttpStatus;
import de.christensen.httpserver.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class CommentController extends HttpController {

    private static final Logger LOG = LoggerFactory.getLogger(CommentController.class);
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json; charset=utf-8";

    private static CommentController instance;
    private final CommentsCollection commentsCollection;

    private CommentController(CommentsCollection commentsCollection) {
        this.commentsCollection = commentsCollection;
    }

    public static CommentController getInstance() {
        if (instance == null) {
            throw new HttpHandlerNotInitializedException();
        }
        return instance;
    }

    public static void initialize(CommentsCollection commentsCollection) {
        instance = new CommentController(commentsCollection);
    }

    public String getComments() {
        try {
            return createCommentsResponse();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String createCommentsResponse() {
        final List<Comment> comments = commentsCollection.getComments();
        String content = JSONUtils.commentsToJSONArray(comments);
        //get latest creationDate, format it to RFC1123 date time and use it as last modified header
        final String lastModified = comments.stream()
                .map(comment -> comment.get_id().getTimestamp())
                .max(Integer::compareTo)
                .map(this::formatDateToRFC1123DateTime)
                .orElseGet(() -> ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        return createResponse(HttpStatus.OK, CONTENT_TYPE_APPLICATION_JSON, content, lastModified);
    }

    public String postComment(String request) {
        try {
            return saveComment(request);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String saveComment(String request) {
        StringBuilder body = parseBody(request);
        Comment comment = JSONUtils.parseBodyToComment(body.toString());
        final Comment savedComment = commentsCollection.saveComment(comment);

        String content = savedComment.toJson();
        final String lastModified = formatDateToRFC1123DateTime(savedComment.get_id().getTimestamp());
        return createResponse(HttpStatus.CREATED, CONTENT_TYPE_APPLICATION_JSON, content, lastModified);
    }

    private StringBuilder parseBody(String request) {
        boolean emptyLine = false;
        StringBuilder body = new StringBuilder();
        for(String line: request.lines().collect(Collectors.toList())){
            if(line.isEmpty() || emptyLine){
                emptyLine = true;
                body.append(line);
            }
        }
        return body;
    }

    private String formatDateToRFC1123DateTime(Integer timeSeconds) {
        return Instant.ofEpochSecond(timeSeconds)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }
}