package com.example.fit_lifegym.models;

import java.util.List;

public class FitnessGoalSubmission {
    private String id;
    private String memberId;
    private String memberName;
    private String selectedGoal;
    private String notes;
    private List<String> photoUrls;
    private long submittedDate;
    private String status; // PENDING, TRAINER_UPDATED, DOCTOR_UPDATED, COMPLETED

    public FitnessGoalSubmission() {}

    public FitnessGoalSubmission(String id, String memberId, String memberName, String selectedGoal, String notes, List<String> photoUrls, long submittedDate) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.selectedGoal = selectedGoal;
        this.notes = notes;
        this.photoUrls = photoUrls;
        this.submittedDate = submittedDate;
        this.status = "PENDING";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getSelectedGoal() { return selectedGoal; }
    public void setSelectedGoal(String selectedGoal) { this.selectedGoal = selectedGoal; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
    public long getSubmittedDate() { return submittedDate; }
    public void setSubmittedDate(long submittedDate) { this.submittedDate = submittedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
