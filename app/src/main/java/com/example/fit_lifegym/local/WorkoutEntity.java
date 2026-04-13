package com.example.fit_lifegym.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.fit_lifegym.models.Exercise;

import java.util.Date;
import java.util.List;

@Entity(tableName = "workouts")
public class WorkoutEntity {
    @PrimaryKey(autoGenerate = true)
    private int localId;
    
    private String id; // Firebase ID
    private String userId;
    private String workoutName;
    private String workoutType;
    private Date startTime;
    private Date endTime;
    private int durationMinutes;
    private int totalCaloriesBurned;
    private int averageHeartRate;
    private int totalSteps;
    private String notes;
    
    @TypeConverters(Converters.class)
    private List<Exercise.ExerciseLog> exercises;
    
    private boolean isCompleted;
    private boolean isSynced; // Flag to track if synced with Firebase

    public WorkoutEntity() {}

    // Getters and Setters
    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getWorkoutName() { return workoutName; }
    public void setWorkoutName(String workoutName) { this.workoutName = workoutName; }
    public String getWorkoutType() { return workoutType; }
    public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }
    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }
    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public int getTotalCaloriesBurned() { return totalCaloriesBurned; }
    public void setTotalCaloriesBurned(int totalCaloriesBurned) { this.totalCaloriesBurned = totalCaloriesBurned; }
    public int getAverageHeartRate() { return averageHeartRate; }
    public void setAverageHeartRate(int averageHeartRate) { this.averageHeartRate = averageHeartRate; }
    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<Exercise.ExerciseLog> getExercises() { return exercises; }
    public void setExercises(List<Exercise.ExerciseLog> exercises) { this.exercises = exercises; }
    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }
    public boolean isSynced() { return isSynced; }
    public void setSynced(boolean synced) { isSynced = synced; }
}
