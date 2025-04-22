package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/stripe-account")
public class StripeAccountController {

    private final UserRepository userRepository;

    public StripeAccountController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get the Stripe account ID for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<String> getStripeAccountId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String userId = authentication.getName();
        Optional<User> userOpt = userRepository.findById(userId);
        return userOpt.map(user -> ResponseEntity.ok(user.getStripeAccountId() != null ? user.getStripeAccountId() : ""))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Create a new Stripe account ID for the authenticated user.
     */
    @PostMapping("/create")
    public ResponseEntity<User> createStripeAccountId(@RequestParam String stripeAccountId,
                                                      Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getStripeAccountId() != null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(user);
            }
            user.setStripeAccountId(stripeAccountId);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Update (or set) the Stripe account ID for the authenticated user.
     */
    @PutMapping
    public ResponseEntity<User> updateStripeAccountId(@RequestParam String stripeAccountId,
                                                      Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStripeAccountId(stripeAccountId);
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    /**
     * Delete the Stripe account ID for the authenticated user.
     */
    @DeleteMapping()
    public ResponseEntity<Void> deleteStripeAccountId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setStripeAccountId(null);
            userRepository.save(user);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
