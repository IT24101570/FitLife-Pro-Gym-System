package com.example.fit_lifegym.models;

import java.util.Date;

public class BodyMeasurement {
    private String id;
    private String userId;
    private Date date;
    private double weight; // kg
    private double height; // cm
    private double bodyFatPercentage;
    private double muscleMass; // kg
    private double bmi;
    
    // Body measurements in cm
    private double chest;
    private double waist;
    private double hips;
    private double biceps;
    private double thighs;
    private double calves;
    
    private String notes;
    private String photoUrl;
    private Date createdAt;

    public BodyMeasurement() {
        this.createdAt = new Date();
    }

    public BodyMeasurement(String userId, Date date, double weight, double height) {
        this.userId = userId;
        this.date = date;
        this.weight = weight;
        this.height = height;
        this.bmi = calculateBMI(weight, height);
        this.createdAt = new Date();
    }

    private double calculateBMI(double weight, double height) {
        if (height > 0) {
            double heightInMeters = height / 100.0;
            return weight / (heightInMeters * heightInMeters);
        }
        return 0;
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

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
        this.bmi = calculateBMI(weight, this.height);
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
        this.bmi = calculateBMI(this.weight, height);
    }

    public double getBodyFatPercentage() {
        return bodyFatPercentage;
    }

    public void setBodyFatPercentage(double bodyFatPercentage) {
        this.bodyFatPercentage = bodyFatPercentage;
    }

    public double getMuscleMass() {
        return muscleMass;
    }

    public void setMuscleMass(double muscleMass) {
        this.muscleMass = muscleMass;
    }

    public double getBmi() {
        return bmi;
    }

    public void setBmi(double bmi) {
        this.bmi = bmi;
    }

    public double getChest() {
        return chest;
    }

    public void setChest(double chest) {
        this.chest = chest;
    }

    public double getWaist() {
        return waist;
    }

    public void setWaist(double waist) {
        this.waist = waist;
    }

    public double getHips() {
        return hips;
    }

    public void setHips(double hips) {
        this.hips = hips;
    }

    public double getBiceps() {
        return biceps;
    }

    public void setBiceps(double biceps) {
        this.biceps = biceps;
    }

    public double getThighs() {
        return thighs;
    }

    public void setThighs(double thighs) {
        this.thighs = thighs;
    }

    public double getCalves() {
        return calves;
    }

    public void setCalves(double calves) {
        this.calves = calves;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getBMICategory() {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    public String getFormattedBMI() {
        return String.format("%.1f (%s)", bmi, getBMICategory());
    }
}
