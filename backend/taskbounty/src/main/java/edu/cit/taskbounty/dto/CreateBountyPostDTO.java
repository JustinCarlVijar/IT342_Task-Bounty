package edu.cit.taskbounty.dto;

import java.util.List;

public class CreateBountyPostDTO {
    private String title;
    private String description;
    private String[] category;
    private String price;
    private boolean isAssigned;
    private String assignedTo;

    // Constructors, getters, setters
    public CreateBountyPostDTO() {}

    public CreateBountyPostDTO(String title, String description, String[] category, String price, boolean isAssigned, String assignedTo) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.price = price;
        this.isAssigned = isAssigned;
        this.assignedTo = assignedTo;
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
}
