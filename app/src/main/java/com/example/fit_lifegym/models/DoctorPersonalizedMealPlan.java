package com.example.fit_lifegym.models;

public class DoctorPersonalizedMealPlan {
    private String id;
    private String submissionId;
    private String doctorId;
    private String doctorName;
    private String breakfastPlan;
    private String lunchPlan;
    private String dinnerPlan;
    private String snacksPlan;
    private String notes;
    private long updatedDate;

    public DoctorPersonalizedMealPlan() {}

    public DoctorPersonalizedMealPlan(String doctorId, String doctorName, String memberId, String breakfastPlan, String lunchPlan, String dinnerPlan, String snacksPlan, String notes) {
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.submissionId = memberId; // Assuming memberId is used as submissionId for simplicity
        this.breakfastPlan = breakfastPlan;
        this.lunchPlan = lunchPlan;
        this.dinnerPlan = dinnerPlan;
        this.snacksPlan = snacksPlan;
        this.notes = notes;
        this.updatedDate = System.currentTimeMillis();
    }

    public String getBreakfast() { return breakfastPlan; }
    public String getLunch() { return lunchPlan; }
    public String getDinner() { return dinnerPlan; }
    public String getSnacks() { return snacksPlan; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubmissionId() { return submissionId; }
    public void setSubmissionId(String submissionId) { this.submissionId = submissionId; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public String getBreakfastPlan() { return breakfastPlan; }
    public void setBreakfastPlan(String breakfastPlan) { this.breakfastPlan = breakfastPlan; }
    public String getLunchPlan() { return lunchPlan; }
    public void setLunchPlan(String lunchPlan) { this.lunchPlan = lunchPlan; }
    public String getDinnerPlan() { return dinnerPlan; }
    public void setDinnerPlan(String dinnerPlan) { this.dinnerPlan = dinnerPlan; }
    public String getSnacksPlan() { return snacksPlan; }
    public void setSnacksPlan(String snacksPlan) { this.snacksPlan = snacksPlan; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public long getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(long updatedDate) { this.updatedDate = updatedDate; }
}
