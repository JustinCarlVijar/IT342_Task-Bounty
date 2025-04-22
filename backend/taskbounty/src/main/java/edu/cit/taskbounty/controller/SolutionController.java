package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.dto.SubmitSolutionDTO;
import edu.cit.taskbounty.model.BountyPost;
import edu.cit.taskbounty.model.Solution;
import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.repository.BountyPostRepository;
import edu.cit.taskbounty.repository.UserRepository;
import edu.cit.taskbounty.service.SolutionService;
import edu.cit.taskbounty.util.JwtUtil;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solutions")
public class SolutionController {

    private final SolutionService solutionService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private BountyPostRepository bountyPostRepository;
    @Autowired
    private UserRepository userRepository;
    public SolutionController(SolutionService solutionService) {
        this.solutionService = solutionService;
    }

    /**
     * Endpoint to submit a solution.
     * POST /solutions/submit
     */
    @PostMapping("/submit")
    public ResponseEntity<Solution> submitSolution(SubmitSolutionDTO submitSolutionDTO) {
        // In a real app, you'd retrieve submitterId from the authenticated user context.
        Solution solution = solutionService.submitSolution(submitSolutionDTO);
        return ResponseEntity.ok(solution);
    }

    /**
     * Endpoint to approve a solution.
     * Only the bounty post creator should be allowed to call this.
     * POST /solutions/{solutionId}/approve?bountyPostId={bountyPostId}
     */
    @PostMapping("/{solutionId}/approve")
    public ResponseEntity<?> approveSolution(@PathVariable String solutionId,
                                                  @RequestParam String bountyPostId,
                                                  @CookieValue(name = "jwt") String authToken) {
        return solutionService.approveSolution(solutionId, bountyPostId, authToken);
    }


    /**
     * Endpoint to update bank credentials for a user.
     * For example, store the user's Stripe connected account ID.
     * POST /solutions/update-bank-credentials?userId={userId}
     */
    @PostMapping("/update-bank-credentials")
    public ResponseEntity<User> updateBankCredentials(@RequestParam String userId,
                                                      @RequestParam String stripeAccountId,
                                                      @CookieValue(name = "jwt") String authToken) {
        User updatedUser = solutionService.updateBankCredentials(userId, stripeAccountId);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSolutionsByBountyPostId(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @CookieValue(name = "jwt") String token){
        String username = jwtUtil.getUserNameFromJwtToken(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        BountyPost bountyPost = bountyPostRepository.findById(new ObjectId(id))
                .orElseThrow(() -> new RuntimeException("Bounty Post not found"));

        if (!bountyPost.getCreatorId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value())).build();
        }

        Page<Solution> solutions = solutionService.getSolutionsByBountyPostId(id, page, size);
        return ResponseEntity.ok(solutions);
    }
}
