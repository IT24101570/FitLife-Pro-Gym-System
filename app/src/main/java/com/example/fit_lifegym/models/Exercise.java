package com.example.fit_lifegym.models;

import java.util.Date;

public class Exercise {
    private String id;
    private String name;
    private String category; // STRENGTH, CARDIO, FLEXIBILITY, SPORTS
    private String muscleGroup; // CHEST, BACK, LEGS, ARMS, SHOULDERS, CORE, FULL_BODY
    private String difficulty; // BEGINNER, INTERMEDIATE, ADVANCED
    private String description;
    private String imageUrl;
    private String videoUrl;
    private int caloriesPerMinute;
    private boolean isFavorite;

    public Exercise() {
    }

    public Exercise(String id, String name, String category, String muscleGroup) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.muscleGroup = muscleGroup;
        this.difficulty = "BEGINNER";
        this.isFavorite = false;
    }
    
    // Inner class for exercise logs in workout sessions
    public static class ExerciseLog {
        private String exerciseName;
        private int sets;
        private int reps;
        private double weight;
        private Date completedAt;

        public ExerciseLog() {
        }

        public String getExerciseName() {
            return exerciseName;
        }

        public void setExerciseName(String exerciseName) {
            this.exerciseName = exerciseName;
        }

        public int getSets() {
            return sets;
        }

        public void setSets(int sets) {
            this.sets = sets;
        }

        public int getReps() {
            return reps;
        }

        public void setReps(int reps) {
            this.reps = reps;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public Date getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(Date completedAt) {
            this.completedAt = completedAt;
        }
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

    public String getMuscleGroup() {
        return muscleGroup;
    }

    public void setMuscleGroup(String muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public int getCaloriesPerMinute() {
        return caloriesPerMinute;
    }

    public void setCaloriesPerMinute(int caloriesPerMinute) {
        this.caloriesPerMinute = caloriesPerMinute;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }
}
