package com.example.fit_lifegym.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

@IgnoreExtraProperties
public class Professional {
    private String id;
    private String name;
    private String type; // TRAINER, DOCTOR
    private String specialization;
    private String imageUrl;
    private double averageRating; // Aligned with Firebase Rules
    private int ratingCount;      // Aligned with Firebase Rules
    private int experience; // years
    private double pricePerSession;
    private boolean isAvailable;
    
    private String workingPlace;
    private String availableLocations;
    private String email;
    private String contactNumber;
    private String bankDetails;
    private String description;
    private String licenseNumber;
    private String approvalStatus; // PENDING, APPROVED, REJECTED
    private double hourlyFee;

    public Professional() {
        this.isAvailable = true;
        this.averageRating = 5.0;
        this.ratingCount = 0;
        this.approvalStatus = "PENDING";
    }

    public Professional(String id, String name, String type, String specialization) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.specialization = specialization;
        this.isAvailable = true;
        this.averageRating = 5.0;
        this.ratingCount = 0;
        this.approvalStatus = "PENDING";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @PropertyName("averageRating")
    public double getRating() { return averageRating; }
    @PropertyName("averageRating")
    public void setRating(double averageRating) { this.averageRating = averageRating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public double getPricePerSession() { return pricePerSession; }
    public void setPricePerSession(double pricePerSession) { this.pricePerSession = pricePerSession; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public String getWorkingPlace() { return workingPlace; }
    public void setWorkingPlace(String workingPlace) { this.workingPlace = workingPlace; }
    public String getAvailableLocations() { return availableLocations; }
    public void setAvailableLocations(String availableLocations) { this.availableLocations = availableLocations; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public String getBankDetails() { return bankDetails; }
    public void setBankDetails(String bankDetails) { this.bankDetails = bankDetails; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public double getHourlyFee() { return hourlyFee; }
    public void setHourlyFee(double hourlyFee) { this.hourlyFee = hourlyFee; }
}
