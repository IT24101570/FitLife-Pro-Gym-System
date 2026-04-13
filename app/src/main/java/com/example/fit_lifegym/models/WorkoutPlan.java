package com.example.fit_lifegym.models;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlan {
    private String id;
    private String name;
    private String description;
    private String difficulty; // Beginner, Intermediate, Advanced
    private String goal; // Muscle Gain, Weight Loss, Endurance
    private int durationWeeks;
    private int workoutsPerWeek;
    private List<String> exerciseIds;
    private String creatorId; // Trainer ID
    private String creatorName;
    private boolean isPremium;

    public WorkoutPlan() {
        this.exerciseIds = new ArrayList<>();
    }

    public WorkoutPlan(String id, String name, String goal, String difficulty) {
        this.id = id;
        this.name = name;
        this.goal = goal;
        this.difficulty = difficulty;
        this.exerciseIds = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public int getDurationWeeks() { return durationWeeks; }
    public void setDurationWeeks(int durationWeeks) { this.durationWeeks = durationWeeks; }
    public int getWorkoutsPerWeek() { return workoutsPerWeek; }
    public void setWorkoutsPerWeek(int workoutsPerWeek) { this.workoutsPerWeek = workoutsPerWeek; }
    public List<String> getExerciseIds() { return exerciseIds; }
    public void setExerciseIds(List<String> exerciseIds) { this.exerciseIds = exerciseIds; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }
}
