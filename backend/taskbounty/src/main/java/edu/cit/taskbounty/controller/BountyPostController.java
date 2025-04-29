package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.dto.BountyPostRequest;
import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.service.BountyPostService;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/bounty_post")
public class BountyPostController {

    private final BountyPostService bountyPostService;

    public BountyPostController(BountyPostService bountyPostService) {
        this.bountyPostService = bountyPostService;
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

        BountyPost createdPost = bountyPostService.createBountyPost(bountyPostRequest);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    // New endpoint to get all draft bounty posts for the current user
    @GetMapping("/draft")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BountyPost>> getDraftBountyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size) {
        try {
            Page<BountyPost> draftPosts = bountyPostService.getDraftBountyPosts(page, size);
            return new ResponseEntity<>(draftPosts, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // New endpoint to get a specific draft bounty post by ID
    @GetMapping("/{id}/draft")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getDraftBountyPost(@PathVariable ObjectId id) {
        try {
            BountyPost draftPost = bountyPostService.getDraftBountyPost(id);
            return new ResponseEntity<>(draftPost, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}/payment-session")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> createPaymentSession(@PathVariable ObjectId id) {
        String checkoutUrl = bountyPostService.createPaymentSession(id);
        return ResponseEntity.ok(checkoutUrl);
    }

    @PostMapping("/{id}/donate")
    public ResponseEntity<String> donateToBounty(@PathVariable ObjectId id, @RequestParam("amount") BigDecimal amount) {
        String checkoutUrl = bountyPostService.createDonationSession(id, amount);
        return ResponseEntity.ok(checkoutUrl);
    }

    @GetMapping("/{id}/payment-success")
    public ResponseEntity<String> paymentSuccess(@PathVariable ObjectId id, @RequestParam("session_id") String sessionId) {
        boolean success = bountyPostService.confirmPayment(id, sessionId);
        if (success) {
            return new ResponseEntity<>("Payment successful, post is now public", HttpStatus.OK);
        }
        return new ResponseEntity<>("Payment failed or session invalid", HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/{id}/donation-success")
    public ResponseEntity<String> confirmDonation(@RequestParam("session_id") String sessionId) {
        try {
            bountyPostService.handleSuccessfulDonation(sessionId);
            return ResponseEntity.ok("Donation successful! Bounty price updated.");
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<Page<BountyPost>> getBountyPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size,
            @RequestParam(defaultValue = "most_upvoted") String sortBy,
            @RequestParam(required = false) String search) {
        // Valid sortBy values: "oldest", "newest", "most_upvoted" (default)
        Page<BountyPost> posts = bountyPostService.getBountyPosts(page, size, sortBy, search);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBountyPost(@PathVariable ObjectId id) {
        try {
            BountyPost post = bountyPostService.getBountyPostById(id);
            return new ResponseEntity<>(post, HttpStatus.OK);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
}