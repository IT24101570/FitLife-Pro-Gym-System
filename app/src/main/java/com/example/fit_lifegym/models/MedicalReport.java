package com.example.fit_lifegym.models;

import java.util.Date;

public class MedicalReport {
    private String id;
    private String bookingId;
    private String memberId;
    private String memberName;
    private String doctorId;
    private String doctorName;
    private Date reportDate;
    
    // Health Metrics
    private String weight;
    private String bloodPressure;
    private String bmi;
    private String heartRate;
    
    // Clinical Notes
    private String symptoms;
    private String diagnosis;
    private String recommendations;
    private String prescribedExercises;
    private String dietaryAdvice;
    
    private Date createdAt;

    public MedicalReport() {
        this.createdAt = new Date();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    public String getMemberId() { return memberId; }
    public void setMemberId(String memberId) { this.memberId = memberId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }
    public Date getReportDate() { return reportDate; }
    public void setReportDate(Date reportDate) { this.reportDate = reportDate; }
    public String getWeight() { return weight; }
    public void setWeight(String weight) { this.weight = weight; }
    public String getBloodPressure() { return bloodPressure; }
    public void setBloodPressure(String bloodPressure) { this.bloodPressure = bloodPressure; }
    public String getBmi() { return bmi; }
    public void setBmi(String bmi) { this.bmi = bmi; }
    public String getHeartRate() { return heartRate; }
    public void setHeartRate(String heartRate) { this.heartRate = heartRate; }
    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    public String getPrescribedExercises() { return prescribedExercises; }
    public void setPrescribedExercises(String prescribedExercises) { this.prescribedExercises = prescribedExercises; }
    public String getDietaryAdvice() { return dietaryAdvice; }
    public void setDietaryAdvice(String dietaryAdvice) { this.dietaryAdvice = dietaryAdvice; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
