package com.example.fit_lifegym.models;

public class FoodItem {
    private String id;
    private String name;
    private String category; // PROTEIN, CARBS, VEGETABLES, FRUITS, DAIRY, SNACKS
    private double servingSize; // in grams
    private String servingUnit; // g, ml, piece, cup, etc.
    private int calories;
    private double protein; // in grams
    private double carbs; // in grams
    private double fats; // in grams
    private double fiber; // in grams
    private String barcode;
    private String imageUrl;
    private boolean isFavorite;

    public FoodItem() {
    }

    public FoodItem(String id, String name, int calories) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.servingSize = 100;
        this.servingUnit = "g";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getServingSize() {
        return servingSize;
    }

    public void setServingSize(double servingSize) {
        this.servingSize = servingSize;
    }

    public String getServingUnit() {
        return servingUnit;
    }

    public void setServingUnit(String servingUnit) {
        this.servingUnit = servingUnit;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    public double getFats() {
        return fats;
    }

    public void setFats(double fats) {
        this.fats = fats;
    }

    public double getFiber() {
        return fiber;
    }

    public void setFiber(double fiber) {
        this.fiber = fiber;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getDisplayInfo() {
        return name + " - " + calories + " cal (" + (int) servingSize + servingUnit + ")";
    }
}
