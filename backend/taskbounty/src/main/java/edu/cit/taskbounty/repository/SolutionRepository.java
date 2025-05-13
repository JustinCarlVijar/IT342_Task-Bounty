package edu.cit.taskbounty.repository;

import edu.cit.taskbounty.model.Solution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface SolutionRepository extends MongoRepository<Solution, String> {

    Solution findByBountyPostIdAndApprovedTrue(String bountyPostId);

    Page<Solution> findByBountyPostId(String bountyPostId, Pageable pageable);

    Page<Solution> findBySubmitterId(String submitterId, Pageable pageable);

    Optional<Solution> findByIdAndBountyPostId(String solutionId, String bountyPostId);
}