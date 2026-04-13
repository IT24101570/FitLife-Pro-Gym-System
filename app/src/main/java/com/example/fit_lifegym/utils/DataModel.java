package com.example.fit_lifegym.utils;

import java.util.UUID;

public class DataModel {
    private String id;
    private String sender;
    private String senderName; // Added field for Display Name
    private String target;
    private DataModelType type;
    private String data;
    private long timeStamp;

    public DataModel() {
        this.id = UUID.randomUUID().toString();
        this.timeStamp = System.currentTimeMillis();
    }

    public DataModel(String sender, String target, DataModelType type, String data) {
        this();
        this.sender = sender;
        this.target = target;
        this.type = type;
        this.data = data;
    }

    public DataModel(String sender, String senderName, String target, DataModelType type, String data) {
        this();
        this.sender = sender;
        this.senderName = senderName;
        this.target = target;
        this.type = type;
        this.data = data;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public DataModelType getType() { return type; }
    public void setType(DataModelType type) { this.type = type; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public long getTimeStamp() { return timeStamp; }
    public void setTimeStamp(long timeStamp) { this.timeStamp = timeStamp; }
}
