package edu.cit.taskbounty.dto;

import org.springframework.web.multipart.MultipartFile;

public class SubmitSolutionDTO {

    private String bountyPostId;
    private String submitterId;
    private String description;
    private MultipartFile[] files;

    SubmitSolutionDTO(){}

    public String getBountyPostId() {
        return bountyPostId;
    }

    public void setBountyPostId(String bountyPostId) {
        this.bountyPostId = bountyPostId;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MultipartFile[] getFiles() {
        return files;
    }

    public void setFiles(MultipartFile[] files) {
        this.files = files;
    }
}
