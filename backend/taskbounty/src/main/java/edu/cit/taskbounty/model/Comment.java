package edu.cit.taskbounty.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "comments")
public class Comment {

    @Id
    private ObjectId id;
    private ObjectId bountyPostId; // Links to the BountyPost
    private ObjectId parentCommentId; // Links to the parent comment (null for top-level)
    private ObjectId authorId; // User who wrote the comment
    private String content; // Comment text (Markdown)
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    // Default constructor
    public Comment() {}

    // Parameterized constructor
    public Comment(ObjectId bountyPostId, ObjectId parentCommentId, ObjectId authorId, String content) {
        this.bountyPostId = bountyPostId;
        this.parentCommentId = parentCommentId;
        this.authorId = authorId;
        this.content = content;
    }

    // Getters and Setters
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public ObjectId getBountyPostId() { return bountyPostId; }
    public void setBountyPostId(ObjectId bountyPostId) { this.bountyPostId = bountyPostId; }
    public ObjectId getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(ObjectId parentCommentId) { this.parentCommentId = parentCommentId; }
    public ObjectId getAuthorId() { return authorId; }
    public void setAuthorId(ObjectId authorId) { this.authorId = authorId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}