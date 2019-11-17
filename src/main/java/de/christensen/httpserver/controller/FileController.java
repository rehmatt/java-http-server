package de.christensen.httpserver.controller;

import de.christensen.httpserver.exceptions.HttpHandlerNotInitializedException;
import de.christensen.httpserver.server.HttpStatus;
import de.christensen.httpserver.utils.JSONUtils;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

public class FileController extends HttpController {

    private static final Logger LOG = LoggerFactory.getLogger(FileController.class);

    private static final int FILE_CONTROLLER_GET_LEADING_CHARS = 9;
    private static final int FILE_CONTROLLER_HEAD_LEADING_CHARS = 10;

    private static FileController instance;
    private final String rootPath;

    private FileController(String rootPath) {
        this.rootPath = rootPath;
    }

    public static FileController getInstance() {
        if (instance == null) {
            throw new HttpHandlerNotInitializedException();
        }
        return instance;
    }

    public static void initialize(String rootPath) {
        instance = new FileController(rootPath);
    }

    public String getStaticContent(String requestedResource) throws IOException {
        try {
            final String pathToStaticResource = "/static" + requestedResource;
            final URI uri = FileController.class.getResource(pathToStaticResource).toURI();
            if (uri.getScheme().equals("jar")) {
                FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                final Path path = fileSystem.getPath(pathToStaticResource);
                final String staticResourceResponse = getStaticResourceResponse(path);
                fileSystem.close();
                return staticResourceResponse;
            } else {
                final Path path = Paths.get(uri);
                return getStaticResourceResponse(path);
            }
        } catch (URISyntaxException | NullPointerException e) {
            LOG.error("Cannot find {}", requestedResource, e);
            return createErrorResponse(HttpStatus.NOT_FOUND);
        }
    }

    private String getStaticResourceResponse(Path path) throws IOException {
        final String content = readFile(path);
        final String contentType = getFileContentType(path);
        final String lastModifiedTime = getLastModifiedTime(path);
        return createResponse(HttpStatus.OK, contentType, content, lastModifiedTime);
    }

    private String getFileContentType(Path path) throws IOException {
        final Tika tika = new Tika();
        return tika.detect(path);
    }

    public String getFile(String request) {
        final String fileUri = getRequestUri(request, FILE_CONTROLLER_GET_LEADING_CHARS);
        return getResponseForPath(rootPath + fileUri, true);
    }

    private String getResponseForPath(String fileUri, boolean isGetRequest) {
        try {
            final Path path = FileSystems.getDefault().getPath(fileUri);
            if (JSONUtils.isDirectory(path)) {
                return getDirectoryResponse(path, isGetRequest);
            } else {
                return getFileResponse(path, isGetRequest);
            }
        } catch (AccessDeniedException e) {
            LOG.error(e.getMessage(), e);
            return createErrorResponse(HttpStatus.ACCESS_DENIED);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getDirectoryResponse(Path path, boolean isGetRequest) throws IOException {
        final String content = JSONUtils.buildJSONObject(path).toString();
        if (isGetRequest) {
            return createResponse(HttpStatus.OK, CONTENT_TYPE_APPLICATION_JSON, content, getLastModifiedTime(path));
        } else {
            return createHeadResponse(HttpStatus.OK, CONTENT_TYPE_APPLICATION_JSON, content, getLastModifiedTime(path));
        }
    }

    private String getLastModifiedTime(Path path) throws IOException {
        //get file or directory last modified time in milliseconds
        final long milliseconds = Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis();
        //create ZonedDateTime object from milliseconds and format to RFC 1123 formatted date time
        return Instant.ofEpochMilli(milliseconds)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.RFC_1123_DATE_TIME);
    }

    private String getFileResponse(Path path, boolean isGetRequest) throws IOException {
        String content = readFile(path).trim();
        final String detectedContentType = getFileContentType(path);
        final String lastModifiedTime = getLastModifiedTime(path);
        if (isGetRequest) {
            return createResponse(HttpStatus.OK, detectedContentType, content, lastModifiedTime);
        } else {
            return createHeadResponse(HttpStatus.OK, detectedContentType, content, lastModifiedTime);
        }
    }

    private String readFile(Path path) throws IOException {
        String content;
        try {
            //try to read the file with UTF8 first
            content = Files.readString(path);
        } catch (MalformedInputException e) {
            //try it one more time with ISO_8859_1
            content = Files.readString(path, StandardCharsets.ISO_8859_1);
        }
        return content.trim();
    }

    public String headFile(String request) {
        final String fileUri = getRequestUri(request, FILE_CONTROLLER_HEAD_LEADING_CHARS);
        return getResponseForPath(rootPath + fileUri, false);
    }
}