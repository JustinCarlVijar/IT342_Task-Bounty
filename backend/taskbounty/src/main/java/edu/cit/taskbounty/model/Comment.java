package edu.cit.taskbounty.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "comments")
public class Comment {
    @Id
    private String id;
    @Field("bountyPostId")
    private String bountyPostId;
    @Field("parentCommentId")
    private String parentCommentId;
    @Field("authorId")
    private String authorId;
    @Field("authorUsername")
    private String authorUsername;
    @Field("content")
    private String content;
    @Field("createdAt")
    private String createdAt;
    @Field("updatedAt")
    private String updatedAt;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBountyPostId() {
        return bountyPostId;
    }

    public void setBountyPostId(String bountyPostId) {
        this.bountyPostId = bountyPostId;
    }

    public String getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(String parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorUsername() {
        return authorUsername;
    }

    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}