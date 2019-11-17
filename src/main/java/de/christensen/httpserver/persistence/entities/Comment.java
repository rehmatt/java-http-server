package de.christensen.httpserver.persistence.entities;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.util.Objects;

public class Comment {

    private ObjectId _id;
    private String text;

    public Comment(String text) {
        this.text = text;
    }

    public Comment(ObjectId id, String text) {
        this(text);
        this._id = id;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Document toDocument() {
        return new Document()
                .append("text", text);
    }

    public String toJson() {
        return JSONObject.wrap(this).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(get_id(), comment.get_id()) &&
                Objects.equals(getText(), comment.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get_id(), getText());
    }
}
