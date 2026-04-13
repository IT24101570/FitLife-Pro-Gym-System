package com.example.fit_lifegym.models;

import java.util.Date;

public class Achievement {
    private String id;
    private String userId;
    private String achievementType;
    private String title;
    private String description;
    private String iconName;
    private int pointsAwarded;
    private Date unlockedDate;
    private boolean isUnlocked;
    private int progress;
    private int target;
    private Date createdAt;

    // Achievement Types
    public static final String FIRST_WORKOUT = "FIRST_WORKOUT";
    public static final String WORKOUTS_10 = "WORKOUTS_10";
    public static final String WORKOUTS_50 = "WORKOUTS_50";
    public static final String WORKOUTS_100 = "WORKOUTS_100";
    public static final String STREAK_7 = "STREAK_7";
    public static final String STREAK_30 = "STREAK_30";
    public static final String CALORIES_1000 = "CALORIES_1000";
    public static final String CALORIES_10000 = "CALORIES_10000";
    public static final String WEIGHT_LIFTED_1000 = "WEIGHT_LIFTED_1000";
    public static final String DISTANCE_10K = "DISTANCE_10K";
    public static final String DISTANCE_100K = "DISTANCE_100K";
    public static final String EARLY_BIRD = "EARLY_BIRD";
    public static final String NIGHT_OWL = "NIGHT_OWL";
    public static final String PERSONAL_RECORD = "PERSONAL_RECORD";
    public static final String CONSISTENCY_KING = "CONSISTENCY_KING";

    public Achievement() {
        this.createdAt = new Date();
        this.isUnlocked = false;
        this.progress = 0;
    }

    public Achievement(String userId, String achievementType) {
        this.userId = userId;
        this.achievementType = achievementType;
        this.createdAt = new Date();
        this.isUnlocked = false;
        this.progress = 0;
        initializeAchievement();
    }

    private void initializeAchievement() {
        switch (achievementType) {
            case FIRST_WORKOUT:
                title = "First Steps";
                description = "Complete your first workout";
                pointsAwarded = 10;
                target = 1;
                break;
            case WORKOUTS_10:
                title = "Getting Started";
                description = "Complete 10 workouts";
                pointsAwarded = 50;
                target = 10;
                break;
            case WORKOUTS_50:
                title = "Dedicated";
                description = "Complete 50 workouts";
                pointsAwarded = 100;
                target = 50;
                break;
            case WORKOUTS_100:
                title = "Century Club";
                description = "Complete 100 workouts";
                pointsAwarded = 200;
                target = 100;
                break;
            case STREAK_7:
                title = "Week Warrior";
                description = "7-day workout streak";
                pointsAwarded = 75;
                target = 7;
                break;
            case STREAK_30:
                title = "Monthly Master";
                description = "30-day workout streak";
                pointsAwarded = 300;
                target = 30;
                break;
            case CALORIES_1000:
                title = "Calorie Crusher";
                description = "Burn 1,000 calories in one workout";
                pointsAwarded = 100;
                target = 1000;
                break;
            case CALORIES_10000:
                title = "Inferno";
                description = "Burn 10,000 total calories";
                pointsAwarded = 250;
                target = 10000;
                break;
            case WEIGHT_LIFTED_1000:
                title = "Iron Lifter";
                description = "Lift 1,000 kg total weight";
                pointsAwarded = 150;
                target = 1000;
                break;
            case DISTANCE_10K:
                title = "10K Runner";
                description = "Run 10 kilometers";
                pointsAwarded = 100;
                target = 10;
                break;
            case DISTANCE_100K:
                title = "Marathon Master";
                description = "Run 100 kilometers total";
                pointsAwarded = 300;
                target = 100;
                break;
            case EARLY_BIRD:
                title = "Early Bird";
                description = "Complete 10 workouts before 7 AM";
                pointsAwarded = 75;
                target = 10;
                break;
            case NIGHT_OWL:
                title = "Night Owl";
                description = "Complete 10 workouts after 9 PM";
                pointsAwarded = 75;
                target = 10;
                break;
            case PERSONAL_RECORD:
                title = "Record Breaker";
                description = "Set a new personal record";
                pointsAwarded = 50;
                target = 1;
                break;
            case CONSISTENCY_KING:
                title = "Consistency King";
                description = "Work out 4+ times per week for a month";
                pointsAwarded = 200;
                target = 16;
                break;
        }
    }

    public void unlock() {
        this.isUnlocked = true;
        this.unlockedDate = new Date();
        this.progress = target;
    }

    public void updateProgress(int newProgress) {
        this.progress = newProgress;
        if (progress >= target && !isUnlocked) {
            unlock();
        }
    }

    public int getProgressPercentage() {
        if (target == 0) return 0;
        return Math.min(100, (progress * 100) / target);
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

    public String getAchievementType() {
        return achievementType;
    }

    public void setAchievementType(String achievementType) {
        this.achievementType = achievementType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getPointsAwarded() {
        return pointsAwarded;
    }

    public void setPointsAwarded(int pointsAwarded) {
        this.pointsAwarded = pointsAwarded;
    }

    public Date getUnlockedDate() {
        return unlockedDate;
    }

    public void setUnlockedDate(Date unlockedDate) {
        this.unlockedDate = unlockedDate;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
