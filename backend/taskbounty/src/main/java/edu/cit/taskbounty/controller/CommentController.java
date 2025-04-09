package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.model.Comment;
import edu.cit.taskbounty.service.CommentService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bounty_post/{postId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * Create a new comment or reply.
     */
    @PostMapping
    public ResponseEntity<Comment> createComment(
            @PathVariable ObjectId postId,
            @RequestBody CommentRequest commentRequest) {
        Comment comment = commentService.createComment(
                postId,
                commentRequest.getParentCommentId(),
                commentRequest.getAuthorId(),
                commentRequest.getContent()
        );
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    /**
     * Get all comments for a BountyPost.
     */
    @GetMapping
    public ResponseEntity<List<Comment>> getCommentsByBountyPostId(@PathVariable ObjectId postId) {
        List<Comment> comments = commentService.getCommentsByBountyPostId(postId);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    /**
     * Get a specific comment by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable ObjectId postId, @PathVariable ObjectId id) {
        Optional<Comment> comment = commentService.getCommentById(id);
        return comment.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Update a comment.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable ObjectId postId,
            @PathVariable ObjectId id,
            @RequestBody CommentRequest commentRequest) {
        Comment comment = new Comment(postId, commentRequest.getParentCommentId(), commentRequest.getAuthorId(), commentRequest.getContent());
        comment.setId(id);
        Comment updatedComment = commentService.updateComment(comment);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    /**
     * Delete a comment and its replies.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable ObjectId postId, @PathVariable ObjectId id) {
        commentService.deleteComment(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

/**
 * DTO for comment creation/update requests.
 */
class CommentRequest {
    private ObjectId parentCommentId;
    private ObjectId authorId;
    private String content;

    // Getters and Setters
    public ObjectId getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(ObjectId parentCommentId) { this.parentCommentId = parentCommentId; }
    public ObjectId getAuthorId() { return authorId; }
    public void setAuthorId(ObjectId authorId) { this.authorId = authorId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}