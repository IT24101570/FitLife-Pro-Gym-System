package com.example.fit_lifegym.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Meal {
    private String id;
    private String userId;
    private String mealType; // BREAKFAST, LUNCH, DINNER, SNACK
    private String name;
    private Date mealTime;
    private List<FoodItem> foodItems;
    private int totalCalories;
    private double totalProtein;
    private double totalCarbs;
    private double totalFats;
    private String notes;
    private String imageUrl;

    public Meal() {
        this.foodItems = new ArrayList<>();
        this.mealTime = new Date();
    }

    public Meal(String userId, String mealType, String name) {
        this.userId = userId;
        this.mealType = mealType;
        this.name = name;
        this.foodItems = new ArrayList<>();
        this.mealTime = new Date();
    }

    // Calculate totals from food items
    public void calculateTotals() {
        totalCalories = 0;
        totalProtein = 0;
        totalCarbs = 0;
        totalFats = 0;

        for (FoodItem item : foodItems) {
            totalCalories += item.getCalories();
            totalProtein += item.getProtein();
            totalCarbs += item.getCarbs();
            totalFats += item.getFats();
        }
    }

    public void addFoodItem(FoodItem item) {
        foodItems.add(item);
        calculateTotals();
    }

    public void removeFoodItem(FoodItem item) {
        foodItems.remove(item);
        calculateTotals();
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

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getMealTime() {
        return mealTime;
    }

    public void setMealTime(Date mealTime) {
        this.mealTime = mealTime;
    }

    public List<FoodItem> getFoodItems() {
        return foodItems;
    }

    public void setFoodItems(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
        calculateTotals();
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }

    public double getTotalProtein() {
        return totalProtein;
    }

    public void setTotalProtein(double totalProtein) {
        this.totalProtein = totalProtein;
    }

    public double getTotalCarbs() {
        return totalCarbs;
    }

    public void setTotalCarbs(double totalCarbs) {
        this.totalCarbs = totalCarbs;
    }

    public double getTotalFats() {
        return totalFats;
    }

    public void setTotalFats(double totalFats) {
        this.totalFats = totalFats;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
