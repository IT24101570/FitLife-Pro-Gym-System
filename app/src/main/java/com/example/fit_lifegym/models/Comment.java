package com.example.fit_lifegym.models;

import java.util.Date;

public class Comment {
    private String id;
    private String userId;
    private String userName;
    private String content;
    private long timestamp;

    public Comment() {
        this.timestamp = new Date().getTime();
    }

    public Comment(String userId, String userName, String content) {
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.timestamp = new Date().getTime();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
