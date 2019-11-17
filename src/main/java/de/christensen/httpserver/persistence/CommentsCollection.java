package de.christensen.httpserver.persistence;

import com.mongodb.client.MongoCollection;
import de.christensen.httpserver.exceptions.HttpHandlerNotInitializedException;
import de.christensen.httpserver.persistence.entities.Comment;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class CommentsCollection {

    private static CommentsCollection instance;
    private final MongoCollection<Document> collection;

    public static void initialize(MongoCollection<Document> collection) {
        instance = new CommentsCollection(collection);
    }

    private CommentsCollection(MongoCollection<Document> collection) {
        this.collection = collection;
    }

    public static CommentsCollection getInstance() {
        if (instance == null) {
            throw new HttpHandlerNotInitializedException();
        }
        return instance;
    }

    public Comment saveComment(Comment comment){
        final Document tDocument = comment.toDocument();
        collection.insertOne(tDocument);
        comment.set_id(tDocument.getObjectId("_id"));
        return comment;
    }

    public List<Comment> getComments(){
        final List<Comment> comments = new ArrayList<>();
        collection.find()
                .map(this::mapDocumentToComment)
                .into(comments);
        return comments;
    }

    private Comment mapDocumentToComment(Document document) {
        final ObjectId id = document.getObjectId("_id");
        final String text = document.getString("text");
        return new Comment(id, text);
    }
}
