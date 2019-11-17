package de.christensen.httpserver;

import de.christensen.httpserver.config.MongoDBClient;
import de.christensen.httpserver.controller.CommentController;
import de.christensen.httpserver.controller.FileController;
import de.christensen.httpserver.persistence.CommentsCollection;
import de.christensen.httpserver.server.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class ServerApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ServerApplication.class);
    private static final String ERROR_UNABLE_TO_LOAD_PROPERTIES = "Unable to load application.properties";

    public static void main(String[] args) throws IOException {
        //use current directory as default path
        String rootPath = ".";
        if (args.length > 0) {
            rootPath = args[0];
        }
        final Properties properties = loadProperties();

        //Initialize components
        MongoDBClient.initialize(properties);
        CommentsCollection.initialize(MongoDBClient.getInstance().getCommentsCollection());
        FileController.initialize(rootPath);
        CommentController.initialize(CommentsCollection.getInstance());

        //Start server
        final HttpServer httpServer = HttpServer.getInstance();
        httpServer.start(properties);
        LOG.debug("Server started");
    }

    /*
     * Load properties from file application.properties in class path.
     *
     * Can be overridden by VM args.
     */
    private static Properties loadProperties() {
        Properties properties = new Properties();

        final ClassLoader classLoader = ServerApplication.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("application.properties")) {
            properties.load(Objects.requireNonNull(inputStream));
        } catch (IOException | NullPointerException e) {
            LOG.error(ERROR_UNABLE_TO_LOAD_PROPERTIES, e);
            throw new RuntimeException(ERROR_UNABLE_TO_LOAD_PROPERTIES);
        }
        properties.keySet()
                .forEach(key -> loadVMArgs(properties, (String) key));

        return properties;
    }

    private static void loadVMArgs(Properties properties, String key) {
        final String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            properties.setProperty(key, systemProperty);
        }
    }

}
