package com.example.fit_lifegym.models;

import com.google.firebase.database.PropertyName;
import java.util.ArrayList;
import java.util.List;

public class MealPlan {
    private String id;
    private String name;
    private String description;
    private String goal; // Weight Loss, Muscle Gain, Maintenance
    private int totalCalories;
    private double protein;
    private double carbs;
    private double fats;
    private List<String> recipes; // List of recipe IDs
    private String creatorId; // Doctor ID
    private String difficulty; // Easy, Moderate, Hard
    private boolean isPremium;

    public MealPlan() {
        this.recipes = new ArrayList<>();
    }

    public MealPlan(String id, String name, String goal, int totalCalories) {
        this.id = id;
        this.name = name;
        this.goal = goal;
        this.totalCalories = totalCalories;
        this.recipes = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @PropertyName("name")
    public String getName() { return name; }
    @PropertyName("name")
    public void setName(String name) { this.name = name; }

    // Fallback mapping for "planName" if used in DB
    @PropertyName("planName")
    public String getPlanName() { return name; }
    @PropertyName("planName")
    public void setPlanName(String planName) { this.name = planName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public int getTotalCalories() { return totalCalories; }
    public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }
    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }
    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }
    public double getFats() { return fats; }
    public void setFats(double fats) { this.fats = fats; }
    public List<String> getRecipes() { return recipes; }
    public void setRecipes(List<String> recipes) { this.recipes = recipes; }
    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }
}
