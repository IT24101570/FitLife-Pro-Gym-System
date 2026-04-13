package com.example.fit_lifegym.models;

import java.util.List;

public class FitnessGoalSubmission {
    private String id;
    private String memberId;
    private String memberName;
    private String goal;
    private String memberNotes;
    private List<String> photoUrls;
    private long timestamp;
    private String status; // PENDING, TRAINER_UPDATED, DOCTOR_UPDATED, COMPLETED

    private String trainerStatus;
    private String doctorStatus;
    private String doctorMealPlan;

    public FitnessGoalSubmission() {}

    public FitnessGoalSubmission(String id, String memberId, String memberName, String goal, String memberNotes, List<String> photoUrls, long timestamp) {
        this.id = id;
        this.memberId = memberId;
        this.memberName = memberName;
        this.goal = goal;
        this.memberNotes = memberNotes;
        this.photoUrls = photoUrls;
        this.timestamp = timestamp;
        this.status = "PENDING";
        this.trainerStatus = "PENDING";
        this.doctorStatus = "PENDING";
    }

    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getMemberNotes() { return memberNotes; }
    public void setMemberNotes(String memberNotes) { this.memberNotes = memberNotes; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDoctorMealPlan() { return doctorMealPlan; }
    public void setDoctorMealPlan(String doctorMealPlan) { this.doctorMealPlan = doctorMealPlan; }

    // Compatibility getters/setters if needed by other parts of the app
    public String getSelectedGoal() { return goal; }
    public void setSelectedGoal(String selectedGoal) { this.goal = selectedGoal; }
    public String getNotes() { return memberNotes; }
    public void setNotes(String notes) { this.memberNotes = notes; }
    public long getSubmittedDate() { return timestamp; }
    public void setSubmittedDate(long submittedDate) { this.timestamp = submittedDate; }

    public String getTrainerStatus() {
        return trainerStatus == null ? "PENDING" : trainerStatus;
    }

    public void setTrainerStatus(String trainerStatus) {
        this.trainerStatus = trainerStatus;
    }

    public String getDoctorStatus() {
        return doctorStatus == null ? "PENDING" : doctorStatus;
    }

    public void setDoctorStatus(String doctorStatus) {
        this.doctorStatus = doctorStatus;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getUserId() { return memberId; } // Alias for memberId
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
