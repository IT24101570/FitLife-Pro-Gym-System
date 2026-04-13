package com.example.fit_lifegym.models;

import java.util.Date;
import java.util.List;

public class WorkoutHistory {
    private String id;
    private String userId;
    private String workoutType;
    private Date date;
    private int duration; // in minutes
    private int caloriesBurned;
    private double distanceKm;
    private List<Exercise.ExerciseLog> exercises;
    private String notes;
    private int setsCompleted;
    private int repsCompleted;
    private double totalWeightLifted;
    private Date createdAt;

    public WorkoutHistory() {
        this.createdAt = new Date();
    }

    public WorkoutHistory(String userId, String workoutType, Date date) {
        this.userId = userId;
        this.workoutType = workoutType;
        this.date = date;
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

    public String getWorkoutType() {
        return workoutType;
    }

    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getCaloriesBurned() {
        return caloriesBurned;
    }

    public void setCaloriesBurned(int caloriesBurned) {
        this.caloriesBurned = caloriesBurned;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public List<Exercise.ExerciseLog> getExercises() {
        return exercises;
    }

    public void setExercises(List<Exercise.ExerciseLog> exercises) {
        this.exercises = exercises;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getSetsCompleted() {
        return setsCompleted;
    }

    public void setSetsCompleted(int setsCompleted) {
        this.setsCompleted = setsCompleted;
    }

    public int getRepsCompleted() {
        return repsCompleted;
    }

    public void setRepsCompleted(int repsCompleted) {
        this.repsCompleted = repsCompleted;
    }

    public double getTotalWeightLifted() {
        return totalWeightLifted;
    }

    public void setTotalWeightLifted(double totalWeightLifted) {
        this.totalWeightLifted = totalWeightLifted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedDuration() {
        int hours = duration / 60;
        int mins = duration % 60;
        if (hours > 0) {
            return hours + "h " + mins + "m";
        }
        return mins + " min";
    }
}
