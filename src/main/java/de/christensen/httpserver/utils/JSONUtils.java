package de.christensen.httpserver.utils;

import de.christensen.httpserver.ServerApplication;
import de.christensen.httpserver.persistence.entities.Comment;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class JSONUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ServerApplication.class);

    private static final String ERROR_MESSAGE_LIST_PATH_EXCEPTION = "An IOException occurred for listing the content of the path {}";

    public static Object buildJSONObject(Path path) {
        final String fileName = path.getFileName().toString();
        final JSONObject jsonObject = new JSONObject();
        if (isDirectory(path)) {
            jsonObject.put(fileName, buildJSONArray(path));
            return jsonObject;
        } else {
            return fileName;
        }
    }

    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
    }

    private static JSONArray buildJSONArray(Path path) {
        try (final Stream<Path> pathStream = Files.list(path)) {
            //map path objects to a JSONObject and collect them as a JSONArray
            return pathStream.map(JSONUtils::buildJSONObject)
                    .collect(Collector.of(
                            JSONArray::new,
                            JSONArray::put,
                            JSONArray::put
                    ));
        } catch (IOException e) {
            LOG.error(ERROR_MESSAGE_LIST_PATH_EXCEPTION, path.getFileName().toString(), e);
            return new JSONArray();
        }
    }

    public static String commentsToJSONArray(List<Comment> comments) {
        final JSONArray commentJSONArray = comments.stream()
                .map(JSONObject::wrap)
                .collect(Collector.of(
                        JSONArray::new,
                        JSONArray::put,
                        JSONArray::put
                ));
        return commentJSONArray.toString();
    }

    public static Comment parseBodyToComment(String body) {
        final JSONObject jsonObject = new JSONObject(body);
        final String text = jsonObject.getString("text");
        return new Comment(text);
    }
}
