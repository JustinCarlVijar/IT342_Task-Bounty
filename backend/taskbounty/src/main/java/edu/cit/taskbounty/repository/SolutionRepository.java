package edu.cit.taskbounty.repository;

import edu.cit.taskbounty.model.Solution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface SolutionRepository extends MongoRepository<Solution, String> {

    // Optionally, you could add methods to find the approved solution for a bounty post
    Solution findByBountyPostIdAndApprovedTrue(String bountyPostId);

    Page<Solution> findByBountyPostId(String bountyPostId, Pageable pageable);
}
