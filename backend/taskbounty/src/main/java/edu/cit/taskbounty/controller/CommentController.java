package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.model.Comment;
import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.BountyPostRepository;
import edu.cit.taskbounty.repository.UserRepository;
import edu.cit.taskbounty.service.CommentService;
import edu.cit.taskbounty.dto.CommentRequest;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BountyPostRepository bountyPostRepository;

    @PostMapping("/{postId}/bounty_post")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comment> createComment(
            @PathVariable("postId") String postId,
            @RequestBody CommentRequest commentRequest) {
        if (!ObjectId.isValid(postId)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String authorId = user.getId();
        Comment comment = commentService.createComment(
                postId,
                commentRequest.getParentCommentId(),
                authorId,
                commentRequest.getContent()
        );
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @GetMapping("/{postId}/bounty_post")
    public ResponseEntity<Page<Comment>> getCommentsByBountyPostId(
            @PathVariable("postId") String postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (!ObjectId.isValid(postId)) {
                System.err.println("Invalid postId format: " + postId);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Comment> comments = commentService.getCommentsByBountyPostId(postId, pageable);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid postId format: " + postId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            System.err.println("Error retrieving comments for postId " + postId + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{postId}/bounty_post/{parentCommentId}")
    public ResponseEntity<List<Comment>> getRepliesByParentCommentId(
            @PathVariable("postId") String postId,
            @PathVariable("parentCommentId") String parentCommentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            if (!ObjectId.isValid(postId) || !ObjectId.isValid(parentCommentId)) {
                System.err.println("Invalid postId or parentCommentId format: postId=" + postId + ", parentCommentId=" + parentCommentId);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            List<Comment> replies = commentService.getRepliesByParentCommentId(postId, parentCommentId, pageable);
            return new ResponseEntity<>(replies, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid postId or parentCommentId format: postId=" + postId + ", parentCommentId=" + parentCommentId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            System.err.println("Error retrieving replies for postId " + postId + ", parentCommentId " + parentCommentId + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }



    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable("id") String id) {
        try {
            if (!ObjectId.isValid(id)) {
                System.err.println("Invalid comment ID format: " + id);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Optional<Comment> comment = commentService.getCommentById(id);
            if (comment.isPresent()) {
                return new ResponseEntity<>(comment.get(), HttpStatus.OK);
            } else {
                System.err.println("Comment not found for ID: " + id);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid comment ID format: " + id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            System.err.println("Error retrieving comment for ID " + id + ": " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Comment> updateComment(
            @PathVariable("commentId") String commentId,
            @RequestBody CommentRequest commentRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String userId = user.getId();
        Optional<Comment> optionalComment = commentService.getCommentById(commentId);
        if (!optionalComment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Comment existingComment = optionalComment.get();
        if (!existingComment.getAuthorId().equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Comment commentToUpdate = new Comment();
        commentToUpdate.setId(commentId);
        commentToUpdate.setContent(commentRequest.getContent());
        Comment updatedComment = commentService.updateComment(commentToUpdate);
        return new ResponseEntity<>(updatedComment, HttpStatus.OK);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable("commentId") String commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String userId = user.getId();
        Optional<Comment> optionalComment = commentService.getCommentById(commentId);
        if (!optionalComment.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Comment comment = optionalComment.get();
        if (!comment.getAuthorId().equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        commentService.deleteComment(commentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}