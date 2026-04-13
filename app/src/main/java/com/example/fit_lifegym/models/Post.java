package com.example.fit_lifegym.models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Post {
    private String id;
    private String userId;
    private String userName;
    private String userProfileImage;
    private String content;
    private String type; // WORKOUT, ACHIEVEMENT, GENERAL
    private String workoutSessionId;
    private long timestamp;
    private Map<String, Boolean> likes = new HashMap<>();
    private int commentCount = 0;

    public Post() {
        this.timestamp = new Date().getTime();
    }

    public Post(String userId, String userName, String content, String type) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.type = type;
        this.timestamp = new Date().getTime();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserProfileImage() { return userProfileImage; }
    public void setUserProfileImage(String userProfileImage) { this.userProfileImage = userProfileImage; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getWorkoutSessionId() { return workoutSessionId; }
    public void setWorkoutSessionId(String workoutSessionId) { this.workoutSessionId = workoutSessionId; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Map<String, Boolean> getLikes() { return likes; }
    public void setLikes(Map<String, Boolean> likes) { this.likes = likes; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public int getLikeCount() {
        return likes != null ? likes.size() : 0;
    }
}
