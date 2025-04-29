package edu.cit.taskbounty.service;

import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.model.Comment;
import edu.cit.taskbounty.repository.BountyPostRepository;
import edu.cit.taskbounty.repository.CommentRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Create a new comment or reply (authenticated users only).
     */
    public Comment createComment(ObjectId bountyPostId, ObjectId parentCommentId, ObjectId authorId, String content) {
        BountyPost post = bountyPostRepository.findById(bountyPostId)
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));

        if (!post.isPublic()) {
            throw new RuntimeException("Cannot comment on a non-public BountyPost");
        }

        if (parentCommentId == null) {
            parentCommentId = new ObjectId();
        }

        Comment comment = new Comment(bountyPostId, parentCommentId, authorId, content);
        comment = commentRepository.save(comment);

        // Update comment count in BountyPost
        post.setCommentCount(post.getCommentCount() + 1);
        bountyPostRepository.save(post);

        return comment;
    }

    /**
     * Get all comments for a BountyPost (public posts only).
     */
    public List<Comment> getCommentsByBountyPostId(ObjectId bountyPostId) {
        BountyPost post = bountyPostRepository.findById(bountyPostId)
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));

        if (!post.isPublic()) {
            throw new RuntimeException("BountyPost is not public");
        }

        return commentRepository.findByBountyPostId(bountyPostId);
    }

    /**
     * Get a comment by ID (public posts only).
     */
    public Optional<Comment> getCommentById(ObjectId id) {
        Optional<Comment> comment = commentRepository.findById(id);
        if (comment.isPresent()) {
            BountyPost post = bountyPostRepository.findById(comment.get().getBountyPostId())
                    .orElseThrow(() -> new RuntimeException("BountyPost not found"));
            if (post.isPublic()) {
                return comment;
            }
        }
        return Optional.empty();
    }

    /**
     * Update a comment (author only).
     */
    @PreAuthorize("isAuthenticated() and #comment.authorId == principal.id")
    public Comment updateComment(Comment comment) {
        Comment existingComment = commentRepository.findById(comment.getId())
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        existingComment.setContent(comment.getContent());
        return commentRepository.save(existingComment);
    }

    /**
     * Delete a comment and its replies (author only).
     */
    @PreAuthorize("isAuthenticated() and #id == principal.id")
    public void deleteComment(ObjectId id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Recursively delete comment and its replies
        deleteCommentAndReplies(comment);

        // Update comment count in BountyPost
        BountyPost post = bountyPostRepository.findById(comment.getBountyPostId())
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));
        int deletedCount = countDeletedComments(id);
        post.setCommentCount(post.getCommentCount() - deletedCount);
        bountyPostRepository.save(post);
    }

    private void deleteCommentAndReplies(Comment comment) {
        List<Comment> replies = commentRepository.findByParentCommentId(comment.getId());
        for (Comment reply : replies) {
            deleteCommentAndReplies(reply);
        }
        commentRepository.delete(comment);
    }

    private int countDeletedComments(ObjectId commentId) {
        int count = 1; // Count the comment itself
        List<Comment> replies = commentRepository.findByParentCommentId(commentId);
        for (Comment reply : replies) {
            count += countDeletedComments(reply.getId());
        }
        return count;
    }
}