package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.dto.BountyPostRequest;
import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.UserRepository;
import edu.cit.taskbounty.service.BountyPostService;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/bounty_post")
public class BountyPostController {

    private final BountyPostService bountyPostService;

    private final UserRepository userRepository;

    public BountyPostController(BountyPostService bountyPostService, UserRepository userRepository) {
        this.bountyPostService = bountyPostService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createBountyPost(@RequestBody BountyPostRequest bountyPostRequest) {
        if (bountyPostRequest.getTitle() == null || bountyPostRequest.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Title cannot be empty.");
        }
        if (bountyPostRequest.getDescription() == null || bountyPostRequest.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Description cannot be empty.");
        }
        if (bountyPostRequest.getBountyPrice() == null || bountyPostRequest.getBountyPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Bounty price must be greater than zero.");
        }

        return bountyPostService.createBountyPost(bountyPostRequest);
    }

    @GetMapping("/draft")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BountyPost>> getDraftBountyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        Page<BountyPost> draftPosts = bountyPostService.getDraftBountyPosts(page, size);
        return new ResponseEntity<>(draftPosts, HttpStatus.OK);
    }

    @GetMapping("/{id}/draft")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDraftBountyPost(@PathVariable ObjectId id) {
        ResponseEntity<BountyPost> response = bountyPostService.getDraftBountyPost(id);
        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody());
        }
        return ResponseEntity.status(response.getStatusCode()).body("Draft bounty post not found or you don't have permission to access it");
    }

    @GetMapping
    public ResponseEntity<Page<BountyPost>> getBountyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "most_upvoted") String sortBy,
            @RequestParam(required = false) String search) {
        Page<BountyPost> posts = bountyPostService.getBountyPosts(page, size, sortBy, search);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("/my_posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyBountyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "most_upvoted") String sortBy,
            @RequestParam(required = false) String search) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(UNAUTHORIZED).body("User not authenticated");
            }

            Page<BountyPost> posts = bountyPostService.getBountyPostsByCreatorId(user.getId(), page, size, sortBy, search);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("Unexpected error fetching bounty posts");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBountyPost(@PathVariable ObjectId id) {
        ResponseEntity<BountyPost> response = bountyPostService.getBountyPostById(id);
        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(response.getBody());
        }
        return ResponseEntity.status(response.getStatusCode()).body("Bounty post not found or you don't have permission to access it");
    }

    @PostMapping("/{id}/vote")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> vote(@PathVariable ObjectId id, @RequestParam("type") String voteType) {
        try {
            boolean success = bountyPostService.vote(id.toString(), voteType);
            if (success) {
                return ResponseEntity.ok(voteType.equalsIgnoreCase("upvote") ? "Upvote recorded" : "Downvote recorded");
            }
            return new ResponseEntity<>("Already " + voteType.toLowerCase() + "d", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid vote type: " + voteType, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteBountyPost(@PathVariable ObjectId id) {
        ResponseEntity<Void> response = bountyPostService.deleteBountyPost(id);
        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok("Bounty post deleted successfully");
        }
        return ResponseEntity.status(response.getStatusCode()).body("Bounty post not found or you don't have permission to delete it");
    }
}