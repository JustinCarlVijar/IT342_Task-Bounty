package edu.cit.taskbounty.service;

import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.model.Comment;
import edu.cit.taskbounty.repository.BountyPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BountyPostService {

    @Autowired
    private BountyPostRepository bountyPostRepository;

    // Create a new Bounty Post
    public BountyPost createBountyPost(BountyPost bountyPost) {
        return bountyPostRepository.save(bountyPost);
    }

    // Get a specific Bounty Post by ID
    public BountyPost getBountyPostById(String id) {
        Optional<BountyPost> bountyPost = bountyPostRepository.findById(id);
        return bountyPost.orElse(null);
    }

    // Get all BountyPosts with optional search and filtering
    public Page<BountyPost> getAllBountyPosts(PageRequest pageRequest, String search, String filter) {
        // Build the query and apply search and filters

        // Apply search logic if needed
        if (search != null && !search.isEmpty()) {
            return bountyPostRepository.findBySearch(pageRequest, search);
        }

        // Apply filter logic (sorting based on filter)
        switch (filter) {
            case "most_upvoted":
                return bountyPostRepository.findAll(pageRequest.withSort(Sort.by(Sort.Order.desc("upvotes"))));
            case "most_downvoted":
                return bountyPostRepository.findAll(pageRequest.withSort(Sort.by(Sort.Order.desc("downvotes"))));
            case "oldest":
                return bountyPostRepository.findAll(pageRequest.withSort(Sort.by(Sort.Order.asc("createdAt"))));
            case "recent":
            default:
                return bountyPostRepository.findAll(pageRequest.withSort(Sort.by(Sort.Order.desc("createdAt"))));
        }
    }

    // Add a comment to a Bounty Post
    public BountyPost addComment(String bountyPostId, Comment comment) {
        BountyPost bountyPost = getBountyPostById(bountyPostId);

        if (bountyPost.isRemoved()) {
            return null;
        }

        if (bountyPost != null) {
            bountyPost.getComments().add(comment);
            return bountyPostRepository.save(bountyPost);
        }
        return null;
    }

    // Upvote a Bounty Post
    public BountyPost upvoteBountyPost(String bountyPostId, String userId) {
        BountyPost bountyPost = getBountyPostById(bountyPostId);

        if (bountyPost.isRemoved()) {
            return null;
        }

        if (bountyPost != null) {
            bountyPost.upvote(userId);
            return bountyPostRepository.save(bountyPost);
        }
        return null;
    }

    // Downvote a Bounty Post
    public BountyPost downvoteBountyPost(String bountyPostId, String userId) {
        BountyPost bountyPost = getBountyPostById(bountyPostId);

        if (bountyPost.isRemoved()) {
            return null;
        }

        if (bountyPost != null) {
            bountyPost.downvote(userId);
            return bountyPostRepository.save(bountyPost);
        }
        return null;
    }

    // Soft delete a Bounty Post (mark as removed)
    public boolean removeBountyPost(String id) {
        BountyPost bountyPost = getBountyPostById(id);
        if (bountyPost != null) {
            bountyPost.setRemoved(true);
            bountyPostRepository.save(bountyPost);
            return true;
        }
        return false;
    }
}
