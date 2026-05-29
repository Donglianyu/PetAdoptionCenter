package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdoptionApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum ApplicationStatus {
        PENDING, APPROVED, REJECTED
    }

    private String applicationId;
    private String petId;
    private String adopterId;
    private String applicantName;
    private String contactInfo;
    private String homeEnvironmentDesc;
    private ApplicationStatus status;
    private LocalDateTime submitTime;
    private String reviewerStaffId;
    private LocalDateTime reviewTime;
    private String rejectReason;

    public AdoptionApplication() {}

    public AdoptionApplication(String applicationId, String petId, String adopterId,
                               String applicantName, String contactInfo, String homeEnvironmentDesc) {
        this.applicationId = applicationId;
        this.petId = petId;
        this.adopterId = adopterId;
        this.applicantName = applicantName;
        this.contactInfo = contactInfo;
        this.homeEnvironmentDesc = homeEnvironmentDesc;
        this.status = ApplicationStatus.PENDING;
        this.submitTime = LocalDateTime.now();
    }

    //
    public void updateStatus(ApplicationStatus newStatus, String staffId, String rejectReason)
            throws exception.InvalidApplicationStatusException {
        if (this.status != ApplicationStatus.PENDING) {
            throw new exception.InvalidApplicationStatusException("Cannot change status of a non-pending application.");
        }
        this.status = newStatus;
        this.reviewerStaffId = staffId;
        this.reviewTime = LocalDateTime.now();
        if (newStatus == ApplicationStatus.REJECTED) {
            this.rejectReason = rejectReason;
        }
    }



    public String getFormattedSubmitTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return submitTime.format(formatter);
    }

    public String getFormattedReviewTime() {
        if (reviewTime == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return reviewTime.format(formatter);
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getPetId() { return petId; }
    public void setPetId(String petId) { this.petId = petId; }

    public String getAdopterId() { return adopterId; }
    public void setAdopterId(String adopterId) { this.adopterId = adopterId; }

    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getHomeEnvironmentDesc() { return homeEnvironmentDesc; }
    public void setHomeEnvironmentDesc(String homeEnvironmentDesc) { this.homeEnvironmentDesc = homeEnvironmentDesc; }

    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }

    public LocalDateTime getSubmitTime() { return submitTime; }
    public void setSubmitTime(LocalDateTime submitTime) { this.submitTime = submitTime; }

    public String getReviewerStaffId() { return reviewerStaffId; }
    public void setReviewerStaffId(String reviewerStaffId) { this.reviewerStaffId = reviewerStaffId; }

    public LocalDateTime getReviewTime() { return reviewTime; }
    public void setReviewTime(LocalDateTime reviewTime) { this.reviewTime = reviewTime; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}

