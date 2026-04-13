package com.example.fit_lifegym.models;

public class TrainerGuidance {
    private String id;
    private String submissionId;
    private String trainerId;
    private String trainerName;
    private String weeklyTarget;
    private String videoLinks;
    private String advice;
    private long updatedDate;

    public TrainerGuidance() {}

    public TrainerGuidance(String trainerId, String trainerName, String submissionId, String weeklyTarget, String advice, String videoLinks) {
        this.trainerId = trainerId;
        this.trainerName = trainerName;
        this.submissionId = submissionId;
        this.weeklyTarget = weeklyTarget;
        this.advice = advice;
        this.videoLinks = videoLinks;
        this.updatedDate = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }
    public String getTrainerId() { return trainerId; }
    public void setTrainerId(String trainerId) { this.trainerId = trainerId; }
    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }
    public String getWeeklyTarget() { return weeklyTarget; }
    public void setWeeklyTarget(String weeklyTarget) { this.weeklyTarget = weeklyTarget; }
    public String getVideoLinks() { return videoLinks; }
    public void setVideoLinks(String videoLinks) { this.videoLinks = videoLinks; }
    public String getAdvice() { return advice; }
    public void setAdvice(String advice) { this.advice = advice; }
    public long getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(long updatedDate) { this.updatedDate = updatedDate; }
}
