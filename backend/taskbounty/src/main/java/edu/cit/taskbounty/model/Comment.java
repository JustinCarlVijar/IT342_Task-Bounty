package edu.cit.taskbounty.model;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {

    private String id;
    private String authorId;
    private String content;
    private Date createdAt;
    private boolean edited;

    public Comment() {}

    public Comment(String id, String authorId, String content, Date createdAt, boolean edited) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
        this.edited = edited;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isEdited() {
        return edited;
    }
    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}
