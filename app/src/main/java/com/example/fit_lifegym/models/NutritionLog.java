package com.example.fit_lifegym.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NutritionLog {
    private String id;
    private String userId;
    private Date date;
    private List<Meal> meals;
    private int waterIntake; // in ml
    private int targetCalories;
    private int targetProtein;
    private int targetCarbs;
    private int targetFats;
    private int targetWater; // in ml
    private String notes;

    public NutritionLog() {
        this.meals = new ArrayList<>();
        this.date = new Date();
        this.waterIntake = 0;
        this.targetWater = 2000; // 2 liters default
    }

    public NutritionLog(String userId, Date date) {
        this.userId = userId;
        this.date = date;
        this.meals = new ArrayList<>();
        this.waterIntake = 0;
        this.targetWater = 2000;
    }

    // Calculate total calories from all meals
    public int getTotalCalories() {
        int total = 0;
        for (Meal meal : meals) {
            total += meal.getTotalCalories();
        }
        return total;
    }

    // Calculate total protein from all meals
    public double getTotalProtein() {
        double total = 0;
        for (Meal meal : meals) {
            total += meal.getTotalProtein();
        }
        return total;
    }

    // Calculate total carbs from all meals
    public double getTotalCarbs() {
        double total = 0;
        for (Meal meal : meals) {
            total += meal.getTotalCarbs();
        }
        return total;
    }

    // Calculate total fats from all meals
    public double getTotalFats() {
        double total = 0;
        for (Meal meal : meals) {
            total += meal.getTotalFats();
        }
        return total;
    }

    // Get remaining calories
    public int getRemainingCalories() {
        return targetCalories - getTotalCalories();
    }

    // Get calorie progress percentage
    public int getCalorieProgress() {
        if (targetCalories == 0) return 0;
        return (int) ((getTotalCalories() * 100.0) / targetCalories);
    }

    // Get water progress percentage
    public int getWaterProgress() {
        if (targetWater == 0) return 0;
        return (int) ((waterIntake * 100.0) / targetWater);
    }

    public void addMeal(Meal meal) {
        meals.add(meal);
    }

    public void removeMeal(Meal meal) {
        meals.remove(meal);
    }

    public void addWater(int ml) {
        waterIntake += ml;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }

    public int getWaterIntake() {
        return waterIntake;
    }

    public void setWaterIntake(int waterIntake) {
        this.waterIntake = waterIntake;
    }

    public int getTargetCalories() {
        return targetCalories;
    }

    public void setTargetCalories(int targetCalories) {
        this.targetCalories = targetCalories;
    }

    public int getTargetProtein() {
        return targetProtein;
    }

    public void setTargetProtein(int targetProtein) {
        this.targetProtein = targetProtein;
    }

    public int getTargetCarbs() {
        return targetCarbs;
    }

    public void setTargetCarbs(int targetCarbs) {
        this.targetCarbs = targetCarbs;
    }

    public int getTargetFats() {
        return targetFats;
    }

    public void setTargetFats(int targetFats) {
        this.targetFats = targetFats;
    }

    public int getTargetWater() {
        return targetWater;
    }

    public void setTargetWater(int targetWater) {
        this.targetWater = targetWater;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
