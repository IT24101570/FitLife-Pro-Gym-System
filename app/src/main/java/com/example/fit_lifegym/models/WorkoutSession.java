package com.example.fit_lifegym.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WorkoutSession {
    private String id;
    private String userId;
    private String workoutName;
    private String workoutType; // General, Strength, Cardio, etc.
    private Date startTime;
    private Date endTime;
    private int durationMinutes;
    private int totalCaloriesBurned;
    private int averageHeartRate; // From wearable
    private int totalSteps; // From wearable
    private String notes;
    private List<Exercise.ExerciseLog> exercises;
    private boolean isCompleted;

    public WorkoutSession() {
        this.exercises = new ArrayList<>();
        this.isCompleted = false;
        this.startTime = new Date();
    }

    public WorkoutSession(String userId, String workoutName) {
        this.userId = userId;
        this.workoutName = workoutName;
        this.exercises = new ArrayList<>();
        this.isCompleted = false;
        this.startTime = new Date();
    }
    
    public void setWorkoutType(String workoutType) {
        this.workoutType = workoutType;
    }
    
    public String getWorkoutType() {
        return workoutType;
    }
    
    public void setExercises(List<Exercise.ExerciseLog> exercises) {
        this.exercises = exercises;
    }
    
    public void setTotalCalories(int calories) {
        this.totalCaloriesBurned = calories;
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

    public String getWorkoutName() {
        return workoutName;
    }

    public void setWorkoutName(String workoutName) {
        this.workoutName = workoutName;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getTotalCaloriesBurned() {
        return totalCaloriesBurned;
    }

    public void setTotalCaloriesBurned(int totalCaloriesBurned) {
        this.totalCaloriesBurned = totalCaloriesBurned;
    }

    public int getAverageHeartRate() {
        return averageHeartRate;
    }

    public void setAverageHeartRate(int averageHeartRate) {
        this.averageHeartRate = averageHeartRate;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Exercise.ExerciseLog> getExercises() {
        return exercises;
    }

    public void addExercise(Exercise.ExerciseLog exercise) {
        this.exercises.add(exercise);
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void completeWorkout() {
        this.isCompleted = true;
        this.endTime = new Date();
        if (startTime != null && endTime != null) {
            this.durationMinutes = (int) ((endTime.getTime() - startTime.getTime()) / 60000);
        }
    }
}
