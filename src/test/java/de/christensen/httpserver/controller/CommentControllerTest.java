package de.christensen.httpserver.controller;

import com.mongodb.MongoTimeoutException;
import de.christensen.httpserver.persistence.CommentsCollection;
import de.christensen.httpserver.persistence.entities.Comment;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CommentsCollection.class)
@PowerMockIgnore({ "javax.management.*", "com.sun.org.apache.xerces.*", "javax.xml.*",
        "org.xml.*", "org.w3c.dom.*", "com.sun.org.apache.xalan.*", "javax.activation.*" })
public class CommentControllerTest {

    @Mock
    private CommentsCollection commentsCollection;

    @Before
    public void setUp() {
        mockStatic(CommentsCollection.class);
        PowerMockito.when(CommentsCollection.getInstance()).thenReturn(commentsCollection);
        CommentController.initialize(CommentsCollection.getInstance());
    }

    @Test
    public void getComments() {
        final Date date = new Date();
        final List<Comment> comments = new ArrayList<>();
        final Comment text1 = new Comment(new ObjectId(date), "text1");
        comments.add(text1);
        final Comment text2 = new Comment(new ObjectId(date), "text2");
        comments.add(text2);
        when(commentsCollection.getComments()).thenReturn(comments);

        final CommentController commentController = CommentController.getInstance();
        final String commentsResponse = commentController.getComments();

        assertThat(commentsResponse.startsWith("HTTP/1.1 200 OK")).isTrue();
        final JSONArray responseContent = new JSONArray(commentsResponse.split("\n\n")[1]);
        assertThat(responseContent.getJSONObject(0).getString("text")).isEqualTo("text1");
        assertThat(responseContent.getJSONObject(0).getJSONObject("_id").getLong("timestamp")).isEqualTo(date.getTime()/1000);
        assertThat(responseContent.getJSONObject(1).getString("text")).isEqualTo("text2");
        assertThat(responseContent.getJSONObject(1).getJSONObject("_id").getLong("timestamp")).isEqualTo(date.getTime()/1000);
    }

    @Test
    public void getCommentsInternalServerError(){
        when(commentsCollection.getComments()).thenThrow(new MongoTimeoutException("timeout"));

        final CommentController commentController = CommentController.getInstance();
        final String response = commentController.getComments();
        assertThat(response.startsWith("HTTP/1.1 500 Internal Server Error")).isTrue();
    }

    @Test
    public void testPostComment() {
        final Date date = new Date();
        final Comment commentToGeSaved = new Comment(null, "savedText");
        final Comment savedComment = new Comment(new ObjectId(date), "savedText");
        when(commentsCollection.saveComment(commentToGeSaved)).thenReturn(savedComment);

        final CommentController commentController = CommentController.getInstance();
        String request =  "POST /api/comments/comment HTTP/1.1\n"
                + "Content-Type: application/json\n\n"
                + "{\"text\":\"savedText\"}";
        final String commentsResponse = commentController.postComment(request);

        assertThat(commentsResponse.startsWith("HTTP/1.1 201 Created")).isTrue();
        final JSONObject responseContent = new JSONObject(commentsResponse.split("\n\n")[1]);
        assertThat(responseContent.getString("text")).isEqualTo("savedText");
        assertThat(responseContent.getJSONObject("_id").getLong("timestamp")).isEqualTo(date.getTime()/1000);
    }

    @Test
    public void testPostCommentInternalServerError(){
        when(commentsCollection.getComments()).thenThrow(new MongoTimeoutException("timeout"));

        final CommentController commentController = CommentController.getInstance();
        String request =  "POST /api/comments/comment HTTP/1.1\n"
                + "Content-Type: application/json\n\n"
                + "{\"text\":\"savedText\"}";
        final String response = commentController.postComment(request);

        assertThat(response.startsWith("HTTP/1.1 500 Internal Server Error")).isTrue();
    }
}