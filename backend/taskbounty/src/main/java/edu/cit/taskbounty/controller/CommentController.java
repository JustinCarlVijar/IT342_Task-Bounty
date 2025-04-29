package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.model.Comment;
import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.UserRepository;
import edu.cit.taskbounty.service.CommentService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/bounty_post/{postId}/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new comment or reply.
     * Gets the authenticated user from security context instead of request body.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comment> createComment(
            @PathVariable("postId") ObjectId postId,
            @RequestBody CommentRequest commentRequest) {
        // Get authenticated username from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Find the user by username
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Convert user ID to ObjectId
        ObjectId authorId = new ObjectId(user.getId());

        Comment comment = commentService.createComment(
                postId,
                commentRequest.getParentCommentId(),
                authorId,
                commentRequest.getContent()
        );
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    /**
     * Get all comments for a BountyPost.
     */
    @GetMapping
    public ResponseEntity<List<Comment>> getCommentsByBountyPostId(@PathVariable("postId") ObjectId postId) {
        List<Comment> comments = commentService.getCommentsByBountyPostId(postId);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    /**
     * Get a specific comment by ID.
     */
    @GetMapping("/{commentId}")
    public ResponseEntity<Comment> getCommentById(
            @PathVariable("postId") ObjectId postId,
            @PathVariable("commentId") ObjectId commentId) {
        Optional<Comment> comment = commentService.getCommentById(commentId);
        return comment.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Update a comment.
     * Uses authenticated user ID for permission check.
     */
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comment> updateComment(
            @PathVariable("postId") ObjectId postId,
            @PathVariable("commentId") ObjectId commentId,
            @RequestBody CommentRequest commentRequest) {
        // Get authenticated username from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Find the user by username
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Convert user ID to ObjectId
        ObjectId authorId = new ObjectId(user.getId());

        Comment comment = new Comment(postId, commentRequest.getParentCommentId(), authorId, commentRequest.getContent());
        comment.setId(commentId);
        Comment updatedComment = commentService.updateComment(comment);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    /**
     * Delete a comment and its replies.
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("postId") ObjectId postId,
            @PathVariable("commentId") ObjectId commentId) {
        commentService.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

/**
 * DTO for comment creation/update requests.
 */
class CommentRequest {
    private String parentCommentId;
    private String content;

    // Getters and Setters
    public ObjectId getParentCommentId() { return new ObjectId(parentCommentId); }
    public void setParentCommentId(ObjectId parentCommentId) { this.parentCommentId = String.valueOf(parentCommentId); }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}