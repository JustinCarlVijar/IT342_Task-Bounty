package edu.cit.taskbounty.controller;

import edu.cit.taskbounty.dto.SubmitSolutionDTO;
import edu.cit.taskbounty.model.Solution;
import edu.cit.taskbounty.model.User;
import edu.cit.taskbounty.service.SolutionService;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/solutions")
public class SolutionController {

    private final SolutionService solutionService;

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
                                                  @RequestHeader("Authorization") String authHeader) {
        return solutionService.approveSolution(solutionId, bountyPostId, authHeader);
    }


    /**
     * Endpoint to update bank credentials for a user.
     * For example, store the user's Stripe connected account ID.
     * POST /solutions/update-bank-credentials?userId={userId}
     */
    @PostMapping("/update-bank-credentials")
    public ResponseEntity<User> updateBankCredentials(@RequestParam String userId,
                                                      @RequestParam String stripeAccountId) {
        User updatedUser = solutionService.updateBankCredentials(userId, stripeAccountId);
        return ResponseEntity.ok(updatedUser);
    }
}
