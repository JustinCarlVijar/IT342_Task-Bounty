package edu.cit.taskbounty.service;

import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.model.Comment;
import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.BountyPostRepository;
import edu.cit.taskbounty.repository.CommentRepository;
import edu.cit.taskbounty.repository.UserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BountyPostRepository bountyPostRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new comment or reply (authenticated users only).
     */
    public Comment createComment(String bountyPostId, String parentCommentId, String authorId, String content) {
        BountyPost post = bountyPostRepository.findById(new ObjectId(bountyPostId))
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));
        if (!post.isPublic()) {
            throw new RuntimeException("Cannot comment on a non-public BountyPost");
        }
        Comment comment = new Comment();
        comment.setParentCommentId(parentCommentId);
        comment.setBountyPostId(bountyPostId);
        comment.setAuthorId(authorId); // Store UUID in database
        // Fetch and set username
        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("User not found for authorId: " + authorId));
        comment.setAuthorUsername(user.getUsername()); // Store username in database
        comment.setContent(content);
        comment = commentRepository.save(comment);
        bountyPostRepository.save(post);
        // Set authorId to username for response
        return comment;
    }

    /**
     * Get paginated comments for a BountyPost (public posts only).
     */
    public Page<Comment> getCommentsByBountyPostId(String bountyPostId, Pageable pageable) {
        System.out.println("Fetching comments for bountyPostId: " + bountyPostId);
        BountyPost post = bountyPostRepository.findById(new ObjectId(bountyPostId))
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));

        if (!post.isPublic()) {
            throw new RuntimeException("BountyPost is not public");
        }

        Page<Comment> comments = commentRepository.findByBountyPostId(bountyPostId, pageable);
        // Map authorId to authorUsername for response
        comments.getContent().forEach(comment -> {
            // Use stored authorUsername if available; otherwise, fetch from userRepository
            String username = comment.getAuthorUsername();
            if (username == null) {
                User user = userRepository.findById(comment.getAuthorId())
                        .orElseThrow(() -> new RuntimeException("User not found for comment: " + comment.getId()));
                username = user.getUsername();
                // Update database to prevent future lookups
                comment.setAuthorUsername(username);
                commentRepository.save(comment);
            }
        });
        System.out.println("Found " + comments.getTotalElements() + " comments for bountyPostId: " + bountyPostId);
        return comments;
    }

    /**
     * Get a comment by ID (public posts only).
     */
    public Optional<Comment> getCommentById(String id) {
        Optional<Comment> comment = commentRepository.findById(new ObjectId(id));
        if (comment.isPresent()) {
            BountyPost post = bountyPostRepository.findById(new ObjectId(comment.get().getBountyPostId()))
                    .orElseThrow(() -> new RuntimeException("BountyPost not found"));
            if (post.isPublic()) {
                // Set authorId to username
                String username = comment.get().getAuthorUsername();
                if (username == null) {
                    User user = userRepository.findById(comment.get().getAuthorId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    username = user.getUsername();
                    comment.get().setAuthorUsername(username);
                    commentRepository.save(comment.get());
                }
                return comment;
            }
        }
        return Optional.empty();
    }

    /**
     * Get replies to a specific comment for a BountyPost.
     */
    public List<Comment> getRepliesByParentCommentId(String postId, String parentCommentId, Pageable pageable) {
        BountyPost post = bountyPostRepository.findById(new ObjectId(postId))
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));
        if (!post.isPublic()) {
            throw new RuntimeException("BountyPost is not public");
        }
        List<Comment> replies = commentRepository.findByParentCommentId(new ObjectId(parentCommentId));
        // Map authorId to authorUsername for response
        replies.forEach(comment -> {
            String username = comment.getAuthorUsername();
            if (username == null) {
                User user = userRepository.findById(comment.getAuthorId())
                        .orElseThrow(() -> new RuntimeException("User not found for comment: " + comment.getId()));
                username = user.getUsername();
                comment.setAuthorUsername(username);
                commentRepository.save(comment);
            }
        });
        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), replies.size());
        return start < replies.size() ? replies.subList(start, end) : List.of();
    }

    /**
     * Update a comment (author only).
     */
    @PreAuthorize("isAuthenticated() and #comment.authorId == principal.id")
    public Comment updateComment(Comment comment) {
        Comment existingComment = commentRepository.findById(new ObjectId(comment.getId()))
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        existingComment.setContent(comment.getContent());
        Comment updatedComment = commentRepository.save(existingComment);
        // Set authorId to username for response
        String username = updatedComment.getAuthorUsername();
        if (username == null) {
            User user = userRepository.findById(updatedComment.getAuthorId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            username = user.getUsername();
            updatedComment.setAuthorUsername(username);
            commentRepository.save(updatedComment);
        }
        return updatedComment;
    }

    /**
     * Delete a comment and its replies (author only).
     */
    @PreAuthorize("isAuthenticated() and #id == principal.id")
    public void deleteComment(String id) {
        Comment comment = commentRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Recursively delete comment and its replies
        deleteCommentAndReplies(comment);

        // Update comment count in BountyPost
        BountyPost post = bountyPostRepository.findById(new ObjectId(comment.getBountyPostId()))
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));
        bountyPostRepository.save(post);
    }

    private void deleteCommentAndReplies(Comment comment) {
        List<Comment> replies = commentRepository.findByParentCommentId(new ObjectId(comment.getId()));
        for (Comment reply : replies) {
            deleteCommentAndReplies(reply);
        }
        commentRepository.delete(comment);
    }
}