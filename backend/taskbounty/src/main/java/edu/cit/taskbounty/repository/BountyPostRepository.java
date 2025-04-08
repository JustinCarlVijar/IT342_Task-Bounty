package edu.cit.taskbounty.repository;

import edu.cit.taskbounty.model.BountyPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface BountyPostRepository extends MongoRepository<BountyPost, String> {
    Page<BountyPost> findAll(Pageable pageable);
    @Query("{ $and: [ { $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } }, { 'category': { $regex: ?0, $options: 'i' } } ] }, { 'removed': { $ne: true } } ] }")
    Page<BountyPost> findBySearch(Pageable pageable, String search);

}
