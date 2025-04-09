package edu.cit.taskbounty.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Payout;
import com.stripe.param.PayoutCreateParams;
import edu.cit.taskbounty.dto.SubmitSolutionDTO;
import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.model.Solution;
import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.BountyPostRepository;
import edu.cit.taskbounty.repository.SolutionRepository;
import edu.cit.taskbounty.repository.UserRepository;
import edu.cit.taskbounty.util.JwtService;
import edu.cit.taskbounty.util.JwtUtil;
import org.apache.coyote.Response;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SolutionService {
    @Autowired
    private final SolutionRepository solutionRepository;
    @Autowired
    private final BountyPostRepository bountyPostRepository;
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final JwtUtil jwtUtil;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public SolutionService(SolutionRepository solutionRepository,
                           BountyPostRepository bountyPostRepository,
                           UserRepository userRepository, JwtService jwtService, JwtUtil jwtUtil) {
        this.solutionRepository = solutionRepository;
        this.bountyPostRepository = bountyPostRepository;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Submit a new solution.
     */
    public Solution submitSolution(SubmitSolutionDTO submittedSolution) {
        String bountyPostId = submittedSolution.getBountyPostId();

        // Check for existing approved solution
        Solution approvedSolution = solutionRepository.findByBountyPostIdAndApprovedTrue(bountyPostId);
        if (approvedSolution != null) {
            throw new RuntimeException("An approved solution already exists for this bounty post.");
        }

        // Define storage directory
        String uploadDir = "files";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory for file uploads: " + uploadDir);
            }
        }
        // Save files and collect paths
        List<Path> savedPaths = new ArrayList<>();
        if (submittedSolution.getFiles() != null) {
            for (MultipartFile file : submittedSolution.getFiles()) {
                try {
                    String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, filename);
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                    savedPaths.add(filePath.toAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to save file: " + file.getOriginalFilename(), e);
                }
            }
        }

        // Build and save the Solution
        Solution solution = new Solution();
        solution.setBountyPostId(submittedSolution.getBountyPostId());
        solution.setSubmitterId(submittedSolution.getSubmitterId());
        solution.setDescription(submittedSolution.getDescription());
        solution.setImagePathList(savedPaths);
        solution.setApproved(false);
        solution.setCreatedAt(LocalDateTime.now());

        return solutionRepository.save(solution);
    }
    /**
     * Approve a solution. Only one solution per bounty post can be approved.
     * Also triggers the payout process.
     */
    public ResponseEntity<?> approveSolution(String solutionId, String bountyPostId, String authHeader) {
        try {
            // Extract username from JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // 400 Bad Request
            }

            String token = authHeader.replace("Bearer ", "");
            String username = jwtUtil.getUserNameFromJwtToken(token);

            // Fetch the bounty post
            BountyPost bountyPost = bountyPostRepository.findById(new ObjectId(bountyPostId))
                    .orElse(null);
            if (bountyPost == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Bounty Post does not exist"); // 404 Not Found
            }

            // Fetch user (bounty creator)
            User user = userRepository.findById(bountyPost.getCreatorId()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist"); // 404 Not Found
            }

            // Ensure the requester is the bounty creator
            if (!bountyPost.getCreatorId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Credentials"); // 403 Forbidden
            }

            // Check if a solution has already been approved
            Solution approved = solutionRepository.findByBountyPostIdAndApprovedTrue(bountyPostId);
            if (approved != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409 Conflict
            }

            // Approve the selected solution
            Solution solution = solutionRepository.findById(solutionId).orElse(null);
            if (solution == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solution doesn't exist"); // 404 Not Found
            }

            solution.setApproved(true);
            solutionRepository.save(solution);

            // Process payout
            processPayout(solution.getSubmitterId(), bountyPost.getBountyPrice());

            return ResponseEntity.ok(solution); // Return approved solution with 200 OK

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // 500 Internal Server Error
        }
    }



    /**
     * Process payout using Stripe. In a real-world scenario, you would need the submitter's connected account or bank details.
     */
    private void processPayout(String submitterId, BigDecimal rewardAmount) {
        // Retrieve the submitter's bank credentials (or connected Stripe account ID)
        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new RuntimeException("Submitter not found."));

        // Assume the user has a field called stripeAccountId or bank details stored.
        if (submitter.getStripeAccountId() == null) {
            throw new RuntimeException("Submitter has not provided bank credentials for payouts.");
        }

        Stripe.apiKey = stripeApiKey;
        // Stripe expects payout amounts in the smallest currency unit (e.g., cents)
        Long amountInCents = rewardAmount.multiply(new BigDecimal("100")).longValue();

        PayoutCreateParams params = PayoutCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("php")  // use appropriate currency
                .setDestination(submitter.getStripeAccountId()) // connected account or bank token
                .build();

        try {
            Payout payout = Payout.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to process payout", e);
        }
    }

    /**
     * Update bank credentials for a user.
     */
    public User updateBankCredentials(String userId, String stripeAccountId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        user.setStripeAccountId(stripeAccountId);
        return userRepository.save(user);
    }
}
