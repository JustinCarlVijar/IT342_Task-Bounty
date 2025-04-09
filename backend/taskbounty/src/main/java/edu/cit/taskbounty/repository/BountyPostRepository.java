package edu.cit.taskbounty.repository;

import edu.cit.taskbounty.model.BountyPost;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BountyPostRepository extends MongoRepository<BountyPost, ObjectId> {
    // Find all public posts with pagination
    Page<BountyPost> findByIsPublicTrue(Pageable pageable);

    // Search public posts by text with pagination
    @Query("{ 'isPublic': true, $text: { $search: ?0 } }")
    Page<BountyPost> searchBountyPosts(String search, Pageable pageable);

    Page<BountyPost> findByCreatorId(String creatorId, Pageable pageable);

    @Query("{ 'creatorId': ?0, $text: { $search: ?1 } }")
    Page<BountyPost> searchMyBountyPosts(String creatorId, String search, Pageable pageable);


}