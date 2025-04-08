package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.dto.CreateBountyPostDTO;
import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.model.Comment;
import edu.cit.taskbounty.security.JwtService;
import edu.cit.taskbounty.service.BountyPostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@RestController
@RequestMapping("/bounty_post")
public class BountyPostController {

    @Autowired
    private BountyPostService bountyPostService;
    @Autowired
    private JwtService jwtService;

    // Create a new Bounty Post
    @PostMapping
    public ResponseEntity<BountyPost> createBountyPost(@RequestBody CreateBountyPostDTO createBountyPostDTO, HttpServletRequest request) {
        // Create BountyPost from DTO
        BountyPost newBountyPost = new BountyPost();
        newBountyPost.setTitle(createBountyPostDTO.getTitle());
        newBountyPost.setDescription(createBountyPostDTO.getDescription());
        newBountyPost.setCategory(createBountyPostDTO.getCategory());
        newBountyPost.setPrice(createBountyPostDTO.getPrice());

        String token = request.getHeader("Authorization").substring(7); // Assuming the token is in the 'Authorization' header with "Bearer" prefix
        String userId = jwtService.extractUsername(token);

        newBountyPost.setCreatedBy(userId);
        newBountyPost.setCreatedAt(new Date());
        newBountyPost.setAssigned(createBountyPostDTO.isAssigned());
        newBountyPost.setAssignedTo(createBountyPostDTO.getAssignedTo());
        newBountyPost.setApproved(false); // Set isApproved to false by default
        newBountyPost.setCompleted(false);
        newBountyPost.setUpvotes(0); // Initialize votes
        newBountyPost.setDownvotes(0);
        newBountyPost.setVotedUsers(new HashSet<>());
        newBountyPost.setComments(new ArrayList<>());
        newBountyPost.setRemoved(false); // Default as not removed

        // Save the new BountyPost
        BountyPost savedBountyPost = bountyPostService.createBountyPost(newBountyPost);

        return new ResponseEntity<>(savedBountyPost, HttpStatus.CREATED);
    }
    // Get a specific Bounty Post by ID
    @GetMapping("/{id}")
    public ResponseEntity<BountyPost> getBountyPostById(@PathVariable String id) {
        BountyPost bountyPost = bountyPostService.getBountyPostById(id);

        if (bountyPost.isRemoved()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return bountyPost != null ?
                new ResponseEntity<>(bountyPost, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    // Get all Bounty Posts with optional search and filtering
    @GetMapping
    public ResponseEntity<Page<BountyPost>> getAllBountyPosts(
            @RequestParam(defaultValue = "0") int page, // Default page is 0 (first page)
            @RequestParam(defaultValue = "20") int size, // Default size is 20
            @RequestParam(required = false) String search, // Optional search
            @RequestParam(defaultValue = "recent") String filter) { // Default filter is 'recent'

        // Pass the parameters to the service layer
        Page<BountyPost> bountyPosts = bountyPostService.getAllBountyPosts(PageRequest.of(page, size), search, filter);

        return new ResponseEntity<>(bountyPosts, HttpStatus.OK);
    }

    // Add a comment to a Bounty Post
    @PostMapping("/{id}/comments")
    public ResponseEntity<BountyPost> addComment(@PathVariable String id, @RequestBody Comment comment) {
        BountyPost updatedBountyPost = bountyPostService.addComment(id, comment);
        return new ResponseEntity<>(updatedBountyPost, HttpStatus.OK);
    }

    // Upvote a Bounty Post
    @PostMapping("/{id}/upvote")
    public ResponseEntity<BountyPost> upvoteBountyPost(@PathVariable String id, @RequestParam String userId) {
        BountyPost updatedBountyPost = bountyPostService.upvoteBountyPost(id, userId);
        return new ResponseEntity<>(updatedBountyPost, HttpStatus.OK);
    }

    // Downvote a Bounty Post
    @PostMapping("/{id}/downvote")
    public ResponseEntity<BountyPost> downvoteBountyPost(@PathVariable String id, @RequestParam String userId) {
        BountyPost updatedBountyPost = bountyPostService.downvoteBountyPost(id, userId);
        return new ResponseEntity<>(updatedBountyPost, HttpStatus.OK);
    }

    // Soft delete a Bounty Post (mark as removed)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeBountyPost(@PathVariable String id) {
        boolean removed = bountyPostService.removeBountyPost(id);
        return removed ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
