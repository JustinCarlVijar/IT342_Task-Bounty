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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;

@Service
public class BountyPostService {

    private static final Logger logger = LoggerFactory.getLogger(BountyPostService.class);

    private final BountyPostRepository bountyPostRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.url}")
    private String url;


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProcessedDonationRepository processedDonationRepository;
    @Autowired
    private JwtUtil jwtUtil;

    public BountyPostService(BountyPostRepository bountyPostRepository) {
        this.bountyPostRepository = bountyPostRepository;
    }

    public Page<BountyPost> getBountyPosts(int page, int size, String sortBy, String search) {
        size = Math.min(size, 25);
        Sort sort;

        // Determine sort order based on sortBy parameter
        sort = switch (sortBy.toLowerCase()) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "upvotes");
        };

        Pageable pageable = PageRequest.of(page, size, sort);

        // Only show public posts (not filtering by creator anymore)
        if (search != null && !search.trim().isEmpty()) {
            // Use only public posts in search
            return bountyPostRepository.searchPublicBountyPosts(search, pageable);
        }

        // Return only public posts
        return bountyPostRepository.findAllPublic(pageable);
    }

    public Page<BountyPost> getDraftBountyPosts(int page, int size) {
        if (!isUserAuthenticated()) {
            throw new AuthenticationRequiredException("Authentication required to access draft posts");
        }

        User user = getCurrentUserId().orElseThrow(() ->
                new RuntimeException("User not found"));

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        return bountyPostRepository.findDraftsByCreatorId(user.getId(), pageable);
    }

    public BountyPost getDraftBountyPost(ObjectId id) {
        if (!isUserAuthenticated()) {
            throw new AuthenticationRequiredException("Authentication required to access draft posts");
        }

        User user = getCurrentUserId().orElseThrow(() ->
                new RuntimeException("User not found"));

        return bountyPostRepository.findDraftByIdAndCreatorId(id, user.getId())
                .orElseThrow(() -> new RuntimeException("Draft bounty post not found or you don't have permission to access it"));
    }

    public BountyPost getBountyPostById(ObjectId id) {
        BountyPost post = bountyPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bounty post not found"));

        // If post is public, return it
        if (post.isPublic()) {
            return post;
        }

        // If post is not public, check if the current user is the creator
        if (!isUserAuthenticated()) {
            throw new AuthenticationRequiredException("Authentication required to access non-public post");
        }

        User currentUser = getCurrentUserId()
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getId().equals(post.getCreatorId())) {
            throw new RuntimeException("You don't have permission to access this post");
        }

        return post;
    }

    public boolean vote(String bountyPostId, String voteType) {
        if (!isUserAuthenticated()) {
            throw new AuthenticationRequiredException("Authentication required to vote");
        }
        Optional<User> userId = getCurrentUserId();
        BountyPost post = bountyPostRepository.findById(new ObjectId(bountyPostId))
                .orElseThrow(() -> new RuntimeException("Bounty post not found"));

        boolean isUpvote = "upvote".equalsIgnoreCase(voteType);
        boolean isDownvote = "downvote".equalsIgnoreCase(voteType);

        if (!isUpvote && !isDownvote) {
            throw new IllegalArgumentException("Invalid vote type: " + voteType);
        }

        if (isUpvote) {
            if (post.getVotedUp().contains(userId)) {
                logger.info("User {} already upvoted post {}", userId, bountyPostId);
                return false;
            }
            if (post.getVotedDown().contains(userId)) {
                post.getVotedDown().remove(userId);
                post.setDownvotes(post.getDownvotes() - 1);
            }
            post.getVotedUp().add(String.valueOf(userId));
            post.setUpvotes(post.getUpvotes() + 1);
            logger.info("User {} upvoted post {}", userId, bountyPostId);
        } else {
            if (post.getVotedDown().contains(userId)) {
                logger.info("User {} already downvoted post {}", userId, bountyPostId);
                return false;
            }
            if (post.getVotedUp().contains(userId)) {
                post.getVotedUp().remove(userId);
                post.setUpvotes(post.getUpvotes() - 1);
            }
            post.getVotedDown().add(String.valueOf(userId));
            post.setDownvotes(post.getDownvotes() + 1);
            logger.info("User {} downvoted post {}", userId, bountyPostId);
        }

        bountyPostRepository.save(post);
        return true;
    }

    private boolean isUserAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }


    private Optional<User> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }

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

    public String createPaymentSession(ObjectId bountyPostId) {
        BountyPost post = bountyPostRepository.findById(bountyPostId)
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));

        if (post.isPublic()) {
            throw new RuntimeException("BountyPost is already public");
        }

        Stripe.apiKey = stripeApiKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(url + "/" + bountyPostId + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(url + "/" + bountyPostId + "/payment-cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("php")
                                                .setUnitAmount(post.getBountyPrice().multiply(new BigDecimal("100")).longValue())
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
            logger.error("Failed to create Stripe session for post {}: {}", bountyPostId, e.getMessage());
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
            if (processedDonationRepository.existsBySessionId(sessionId)) {
                throw new RuntimeException("This donation has already been processed.");
            }

            Session session = Session.retrieve(sessionId);

            if (!"complete".equals(session.getStatus())) {
                throw new RuntimeException("Payment not completed. Bounty price not updated.");
            }

            String bountyPostId = session.getMetadata().get("bountyPostId");
            PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
            BigDecimal donationAmount = BigDecimal.valueOf(paymentIntent.getAmount()).divide(new BigDecimal("100"));

            BountyPost post = bountyPostRepository.findById(new ObjectId(bountyPostId))
                    .orElseThrow(() -> new RuntimeException("BountyPost not found"));

            post.topUpBounty(donationAmount);
            bountyPostRepository.save(post);

            processedDonationRepository.save(new ProcessedDonation(sessionId));
        } catch (StripeException e) {
            logger.error("Failed to process donation for session {}: {}", sessionId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Stripe session", e);
        }
    }

    public String createDonationSession(ObjectId bountyPostId, BigDecimal amount) {
        BountyPost post = bountyPostRepository.findById(bountyPostId)
                .orElseThrow(() -> new RuntimeException("BountyPost not found"));

        Stripe.apiKey = stripeApiKey;

        long amountInCents = amount.multiply(new BigDecimal("100")).longValue();


        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(url + "/" + bountyPostId + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(url + "/" + bountyPostId + "/payment-cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("php")
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
            return session.getUrl();
        } catch (StripeException e) {
            logger.error("Failed to create donation session for post {}: {}", bountyPostId, e.getMessage());
            throw new RuntimeException("Failed to create Stripe session", e);
        }
    }

    public boolean confirmPayment(ObjectId bountyPostId, String sessionId) {
        Stripe.apiKey = stripeApiKey;

        try {
            Session session = Session.retrieve(sessionId);
            if ("paid".equals(session.getPaymentStatus()) && session.getMetadata().get("bountyPostId").equals(bountyPostId.toString())) {
                BountyPost post = bountyPostRepository.findById(bountyPostId)
                        .orElseThrow(() -> new RuntimeException("BountyPost not found"));
                post.setPublic(true);
                bountyPostRepository.save(post);
                logger.info("Payment confirmed for post {}, set to public", bountyPostId);
                return true;
            }
            logger.warn("Payment confirmation failed for post {}, session {}", bountyPostId, sessionId);
            return false;
        } catch (StripeException e) {
            logger.error("Failed to confirm payment for post {}: {}", bountyPostId, e.getMessage());
            throw new RuntimeException("Failed to retrieve Stripe session", e);
        }
    }

    public boolean deleteBountyPost(ObjectId id) {
        if (!isUserAuthenticated()) {
            throw new AuthenticationRequiredException("Authentication required to delete a bounty post");
        }

        User user = getCurrentUserId().orElseThrow(() ->
                new RuntimeException("User not found"));

        BountyPost post = bountyPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bounty post not found"));

        // Check if the user owns the post
        if (!post.getCreatorId().equals(user.getId())) {
            throw new RuntimeException("You don't have permission to delete this bounty post");
        }

        // Delete the post
        bountyPostRepository.delete(post);
        logger.info("User {} deleted bounty post {}", user.getId(), id);
        return true;
    }
}