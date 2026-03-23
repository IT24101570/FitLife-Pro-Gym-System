package com.example.fit_lifegym.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.Date;

@IgnoreExtraProperties
public class Booking {
    private String id;
    private String userId;
    private String userName;
    private String professionalId;
    private String professionalName;
    private String serviceType;
    private Date date; // Matches 'date' in Firebase
    private String timeSlot;
    private String status; // PENDING, CONFIRMED, CANCELLED, COMPLETED
    private double price;
    private String notes;
    private String healthCondition;
    private int rating;
    private String review;
    private Date createdAt;

    public Booking() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getProfessionalId() { return professionalId; }
    public void setProfessionalId(String professionalId) { this.professionalId = professionalId; }
    public String getProfessionalName() { return professionalName; }
    public void setProfessionalName(String professionalName) { this.professionalName = professionalName; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    
    @Exclude
    public Date getBookingDate() { return date; }
    @Exclude
    public void setBookingDate(Date date) { this.date = date; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getHealthCondition() { return healthCondition; }
    public void setHealthCondition(String healthCondition) { this.healthCondition = healthCondition; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    @Exclude
    public boolean isPending() { return "PENDING".equalsIgnoreCase(status); }
    @Exclude
    public boolean isConfirmed() { return "CONFIRMED".equalsIgnoreCase(status); }
    @Exclude
    public boolean isCancelled() { return "CANCELLED".equalsIgnoreCase(status); }
    @Exclude
    public boolean isCompleted() { return "COMPLETED".equalsIgnoreCase(status); }
}
