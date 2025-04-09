package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.dto.BountyPostRequest;
import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.service.BountyPostService;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

    /**
     * Create a new bounty post.
     */
    @PostMapping
    public ResponseEntity<?> createBountyPost(@RequestBody BountyPostRequest bountyPostRequest) {
        // Validate input fields
        if (bountyPostRequest.getTitle() == null || bountyPostRequest.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Title cannot be empty.");
        }
        if (bountyPostRequest.getDescription() == null || bountyPostRequest.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Description cannot be empty.");
        }
        if (bountyPostRequest.getBountyPrice() == null || bountyPostRequest.getBountyPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Bounty price must be greater than zero.");
        }

        // Create the bounty post if validation passes
        BountyPost createdPost = bountyPostService.createBountyPost(bountyPostRequest);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    /**
     * Get a Stripe Checkout Session ID for payment.
     */
    @GetMapping("/{id}/payment-session")
    public ResponseEntity<String> createPaymentSession(@PathVariable ObjectId id) {
        String checkoutUrl = bountyPostService.createPaymentSession(id);
        return ResponseEntity.ok(checkoutUrl);
    }


    /**
     * Create a Stripe checkout session for donating to a bounty post.
     * @param id The ID of the bounty post.
     * @param amount The donation amount.
     * @return The checkout URL.
     */
    @PostMapping("/{id}/donate")
    public ResponseEntity<String> donateToBounty(@PathVariable ObjectId id, @RequestParam("amount") BigDecimal amount) {
        String checkoutUrl = bountyPostService.createDonationSession(id, amount);
        return ResponseEntity.ok(checkoutUrl);
    }

    /**
     * Handle successful payment and make the post public.
     */
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
            @RequestParam(defaultValue = "all") String scope,
            @RequestParam(defaultValue = "most_upvoted") String sortBy,
            @RequestParam(required = false) String search) {
        Page<BountyPost> posts = bountyPostService.getBountyPosts(page, size, scope, sortBy, search);
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

}