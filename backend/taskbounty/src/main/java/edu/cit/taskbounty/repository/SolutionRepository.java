package edu.cit.taskbounty.repository;

import edu.cit.taskbounty.model.Solution;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SolutionRepository extends MongoRepository<Solution, String> {
    // Find all solutions for a given bounty post
    List<Solution> findByBountyPostId(String bountyPostId);

    // Optionally, you could add methods to find the approved solution for a bounty post
    Solution findByBountyPostIdAndApprovedTrue(String bountyPostId);
}
