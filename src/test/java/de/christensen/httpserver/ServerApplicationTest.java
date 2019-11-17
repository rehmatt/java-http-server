package de.christensen.httpserver;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.christensen.httpserver.config.MongoDBClient;
import de.christensen.httpserver.controller.CommentController;
import de.christensen.httpserver.controller.FileController;
import de.christensen.httpserver.persistence.CommentsCollection;
import de.christensen.httpserver.server.HttpServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*",
        "org.xml.*", "org.w3c.dom.*", "com.sun.org.apache.xalan.*", "javax.activation.*" })
@PrepareForTest(value = {MongoClients.class})
public class ServerApplicationTest {

    @Test
    public void testMain() throws InterruptedException, IOException {
        //prepare mocks
        final MongoClient mongoClient = mock(MongoClient.class);
        final MongoDatabase mongoDatabase = mock(MongoDatabase.class);
        when(mongoClient.getDatabase(anyString())).thenReturn(mongoDatabase);
        mockStatic(MongoClients.class);
        when(MongoClients.create(eq("mongodb://localhost:27017"))).thenReturn(mongoClient);

        //start up application
        final String[] args = {};
        final Thread server = new Thread(() -> {
            try {
                ServerApplication.main(args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start();

        sleep(1000);

        //test that all components are initialized
        final MongoDBClient mongoDBClient = MongoDBClient.getInstance();
        assertThat(mongoDBClient).isNotNull();
        final CommentsCollection commentsCollection = CommentsCollection.getInstance();
        assertThat(commentsCollection).isNotNull();
        final FileController fileController = FileController.getInstance();
        assertThat(fileController).isNotNull();
        final CommentController commentController = CommentController.getInstance();
        assertThat(commentController).isNotNull();
        final HttpServer httpServer = HttpServer.getInstance();
        assertThat(httpServer).isNotNull();

        server.interrupt();
    }

}
