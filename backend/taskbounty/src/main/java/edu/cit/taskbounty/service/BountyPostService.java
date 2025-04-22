package edu.cit.taskbounty.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import edu.cit.taskbounty.dto.BountyPostRequest;
import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.model.ProcessedDonation;
import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.BountyPostRepository;
import edu.cit.taskbounty.repository.ProcessedDonationRepository;
import edu.cit.taskbounty.repository.UserRepository;
import edu.cit.taskbounty.util.JwtUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BountyPostService {

    private final BountyPostRepository bountyPostRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProcessedDonationRepository processedDonationRepository;
    @Autowired
    private JwtUtil jwtUtil;

    public BountyPostService(BountyPostRepository bountyPostRepository) {
        this.bountyPostRepository = bountyPostRepository;
    }

    public Page<BountyPost> getBountyPosts(int page, int size, String scope, String sortBy, String search) {
        // Enforce maximum size
        size = Math.min(size, 25);

        // Define sorting
        Sort sort = switch (sortBy.toLowerCase()) {
            case "most_upvoted" -> Sort.by(Sort.Direction.DESC, "upvotes");
            case "most_downvoted" -> Sort.by(Sort.Direction.DESC, "downvotes");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "upvotes"); // Default: most_upvoted
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        if ("mine".equalsIgnoreCase(scope)) {
            if (!isUserAuthenticated()) {
                throw new AuthenticationRequiredException("Authentication required for 'My Bounty Posts'");
            }
            String creatorId = String.valueOf(getCurrentUserId());
            if (search != null && !search.trim().isEmpty()) {
                return bountyPostRepository.searchMyBountyPosts(creatorId, search, pageable);
            }
            return bountyPostRepository.findByCreatorId(creatorId, pageable);
        } else { // scope = "all" or invalid
            if (search != null && !search.trim().isEmpty()) {
                return bountyPostRepository.searchBountyPosts(search, pageable);
            }
            return bountyPostRepository.findByIsPublicTrue(pageable);
        }
    }

    private boolean isUserAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    private ObjectId getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User must be authenticated to create a bounty post");
        }
        // Assuming your UserDetails implementation has an ObjectId ID
        // Adjust this based on your actual User class
        return new ObjectId(auth.getName()); // Example: assumes ID is in 'name' field
    }

    /**
     * Create a new bounty post with isPublic set to false.
     */
    public BountyPost createBountyPost(BountyPostRequest bountyPostRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User must be authenticated to create a bounty post");
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found in repository"));

        BountyPost bountyPost = new BountyPost();
        bountyPost.setTitle(bountyPostRequest.getTitle());
        bountyPost.setDescription(bountyPostRequest.getDescription());
        bountyPost.setBountyPrice(bountyPostRequest.getBountyPrice());
        bountyPost.setCreatorId(user.getId());
        bountyPost.setPublic(false);

        return bountyPostRepository.save(bountyPost);
    }

    /**
     * Create a Stripe Checkout Session for payment.
     */

    public String createPaymentSession(ObjectId bountyPostId) {
        BountyPost post = bountyPostRepository.findById(bountyPostId)
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));

        if (post.isPublic()) {
            throw new RuntimeException("BountyPost is already public");
        }

        Stripe.apiKey = stripeApiKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/bounty_post/" + bountyPostId + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:8080/bounty_post/" + bountyPostId + "/payment-cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("php")
                                                .setUnitAmount(post.getBountyPrice().multiply(new BigDecimal("100")).longValue()) // Convert to cents
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Bounty Post: " + post.getTitle())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("bountyPostId", bountyPostId.toString())
                .build();

        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Failed to create Stripe session", e);
        }
    }

    public void addDonation(ObjectId bountyPostId, BigDecimal amount) {
        BountyPost post = bountyPostRepository.findById(bountyPostId)
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));

        post.topUpBounty(amount);
        bountyPostRepository.save(post);
    }


    public void handleSuccessfulDonation(String sessionId) {
        try {
            // Check if this session ID was already processed
            if (processedDonationRepository.existsBySessionId(sessionId)) {
                throw new RuntimeException("This donation has already been processed.");
            }

            // Retrieve the session from Stripe
            Session session = Session.retrieve(sessionId);

            if (!"complete".equals(session.getStatus())) {
                throw new RuntimeException("Payment not completed. Bounty price not updated.");
            }

            String bountyPostId = session.getMetadata().get("bountyPostId");
            PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
            BigDecimal donationAmount = BigDecimal.valueOf(paymentIntent.getAmount()).divide(new BigDecimal("100"));

            // Find the bounty post
            BountyPost post = bountyPostRepository.findById(new ObjectId(bountyPostId))
                    .orElseThrow(() -> new RuntimeException("BountyPost not found"));

            // Increase bounty price
            post.topUpBounty(donationAmount);
            bountyPostRepository.save(post);

            // **Mark session as processed**
            processedDonationRepository.save(new ProcessedDonation(sessionId));

        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve Stripe session", e);
        }
    }


    /**
     * Create a Stripe Checkout session for donating to a bounty post.
     * @param bountyPostId The ID of the bounty post.
     * @param amount The amount to donate.
     * @return The checkout URL.
     */
    public String createDonationSession(ObjectId bountyPostId, BigDecimal amount) {
        BountyPost post = bountyPostRepository.findById(bountyPostId)
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));

        Stripe.apiKey = stripeApiKey;

        // Stripe requires amounts in cents (smallest currency unit)
        long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/bounty_post/" + bountyPostId + "/donation-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:8080/bounty_post/" + bountyPostId + "/donation-cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("php")  // Change to your currency
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Donation to Bounty Post: " + post.getTitle())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("bountyPostId", bountyPostId.toString())
                .putMetadata("donationAmount", amount.toString())
                .build();

        try {
            Session session = Session.create(params);
            return session.getUrl(); // Return the Stripe checkout page URL
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe session", e);
        }
    }

    /**
     * Confirm payment and make the post public.
     */
    public boolean confirmPayment(ObjectId bountyPostId, String sessionId) {
        Stripe.apiKey = stripeApiKey;

        try {
            Session session = Session.retrieve(sessionId);
            if ("paid".equals(session.getPaymentStatus()) && session.getMetadata().get("bountyPostId").equals(bountyPostId.toString())) {
                BountyPost post = bountyPostRepository.findById(bountyPostId)
                        .orElseThrow(() -> new RuntimeException("BountyPost not found"));
                post.setPublic(true);
                bountyPostRepository.save(post);
                return true;
            }
            return false;
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve Stripe session", e);
        }
    }

    public boolean upvote(String bountyPostId, String token){
        BountyPost bountyPost = bountyPostRepository.findById(new ObjectId(bountyPostId))
                .orElseThrow(() -> new RuntimeException("Bounty post not found"));
        // TODO: Chamge Authorization verification in jwtfilter
        return false;
    }

}

