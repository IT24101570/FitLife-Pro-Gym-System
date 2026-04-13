package com.example.fit_lifegym.models;

public class VideoClass {
    private String id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private String videoUrl;
    private String category; // Yoga, HIIT, Strength, Cardio
    private String instructorName;
    private String instructorProfileId;
    private int durationMinutes;
    private String difficulty; // Beginner, Intermediate, Advanced
    private int views;
    private boolean isPremium;

    public VideoClass() {}

    public VideoClass(String title, String category, String instructorName, int durationMinutes, String difficulty, boolean isPremium) {
        this.title = title;
        this.category = category;
        this.instructorName = instructorName;
        this.durationMinutes = durationMinutes;
        this.difficulty = difficulty;
        this.isPremium = isPremium;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getInstructorName() { return instructorName; }
    public void setInstructorName(String instructorName) { this.instructorName = instructorName; }
    public String getInstructorProfileId() { return instructorProfileId; }
    public void setInstructorProfileId(String instructorProfileId) { this.instructorProfileId = instructorProfileId; }
    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public boolean isPremium() { return isPremium; }
    public void setPremium(boolean premium) { isPremium = premium; }
}
