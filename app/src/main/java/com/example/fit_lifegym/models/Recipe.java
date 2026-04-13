package com.example.fit_lifegym.models;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private String id;
    private String name;
    private String description;
    private String category; // Breakfast, Lunch, Dinner, Snack
    private int calories;
    private double protein;
    private double carbs;
    private double fats;
    private List<String> ingredients;
    private List<String> instructions;
    private String imageUrl;
    private int prepTimeMinutes;
    private boolean isPremium;

    public Recipe() {
        this.ingredients = new ArrayList<>();
        this.instructions = new ArrayList<>();
    }

    public Recipe(String id, String name, int calories) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.ingredients = new ArrayList<>();
        this.instructions = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public int getCalories() { return calories; }
    public void setCalories(int calories) { this.calories = calories; }
    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }
    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }
    public double getFats() { return fats; }
    public void setFats(double fats) { this.fats = fats; }
    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }
    public List<String> getInstructions() { return instructions; }
    public void setInstructions(List<String> instructions) { this.instructions = instructions; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getPrepTimeMinutes() { return prepTimeMinutes; }
    public void setPrepTimeMinutes(int prepTimeMinutes) { this.prepTimeMinutes = prepTimeMinutes; }
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }
}
