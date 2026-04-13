package com.example.fit_lifegym.models;

import java.util.Date;

public class PersonalRecord {
    private String id;
    private String userId;
    private String exerciseName;
    private String recordType; // MAX_WEIGHT, MAX_REPS, MAX_DISTANCE, FASTEST_TIME
    private double value;
    private String unit; // kg, lbs, km, miles, seconds
    private Date achievedDate;
    private String notes;
    private Date createdAt;

    public PersonalRecord() {
        this.createdAt = new Date();
    }

    public PersonalRecord(String userId, String exerciseName, String recordType, double value, String unit) {
        this.userId = userId;
        this.exerciseName = exerciseName;
        this.recordType = recordType;
        this.value = value;
        this.unit = unit;
        this.achievedDate = new Date();
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Date getAchievedDate() {
        return achievedDate;
    }

    public void setAchievedDate(Date achievedDate) {
        this.achievedDate = achievedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedValue() {
        if (recordType.equals("FASTEST_TIME")) {
            int minutes = (int) (value / 60);
            int seconds = (int) (value % 60);
            return String.format("%d:%02d", minutes, seconds);
        }
        return String.format("%.1f %s", value, unit);
    }

    public String getRecordTypeDisplay() {
        switch (recordType) {
            case "MAX_WEIGHT": return "Max Weight";
            case "MAX_REPS": return "Max Reps";
            case "MAX_DISTANCE": return "Max Distance";
            case "FASTEST_TIME": return "Fastest Time";
            default: return recordType;
        }
    }
}
