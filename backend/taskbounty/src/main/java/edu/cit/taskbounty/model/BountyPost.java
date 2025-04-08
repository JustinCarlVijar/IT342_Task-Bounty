package edu.cit.taskbounty.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Document(collection = "bountyPosts")
public class BountyPost {

    @Id
    private String id;
    private String title;
    private String description;
    private String category[];
    private String price;
    private Date createdAt;
    private String createdBy;
    private boolean isAssigned;
    private String assignedTo;
    private boolean isApproved;
    private boolean isCompleted;

    // Voting system
    private int upvotes;
    private int downvotes;
    private Set<String> votedUsers = new HashSet<>();

    // Comment System
    private List<Comment> comments = new ArrayList<>();

    // Soft delete flag
    private boolean removed;

    public BountyPost() {}

    public BountyPost(String id, String title, String description, String[] category, String price, Date createdAt, String createdBy, boolean isAssigned, String assignedTo, boolean isApproved, boolean isCompleted, int upvotes, int downvotes, Set<String> votedUsers, List<Comment> comments, boolean removed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.isAssigned = isAssigned;
        this.assignedTo = assignedTo;
        this.isApproved = isApproved;
        this.isCompleted = isCompleted;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.votedUsers = votedUsers;
        this.comments = comments;
        this.removed = removed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String[] getCategory() {
        return category;
    }

    public void setCategory(String[] category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isAssigned() {
        return isAssigned;
    }

    public void setAssigned(boolean assigned) {
        isAssigned = assigned;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public void upvote(String userId) {
        if (!votedUsers.contains(userId)) {
            this.upvotes++;
            this.votedUsers.add(userId);
        }
    }

    public void downvote(String userId) {
        if (!votedUsers.contains(userId)) {
            this.downvotes++;
            this.votedUsers.add(userId);
        }
    }

    public Set<String> getVotedUsers() {
        return votedUsers;
    }

    public void setVotedUsers(Set<String> votedUsers) {
        this.votedUsers = votedUsers;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}
