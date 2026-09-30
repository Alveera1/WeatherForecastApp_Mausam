package com.example.mausam.models;

import com.google.firebase.firestore.DocumentId;
import java.io.Serializable;

public class Plan implements Serializable {

    @DocumentId
    private String id;          // Auto-populated with Firestore document ID
    private String userId;      // Tied to FirebaseAuth uid
    private String activity;    // e.g. "Outdoor Picnic", "Travel"
    private String location;    // e.g. "Pune"
    private long dateTime;      // Epoch millis when plan is scheduled
    private String status;      // "pending", "safe", "risky"
    private long createdAt;     // Epoch millis when plan document was created

    // Required empty constructor for Firestore deserialization
    public Plan() {
    }

    public Plan(String userId, String activity, String location, long dateTime) {
        this.userId = userId;
        this.activity = activity;
        this.location = location;
        this.dateTime = dateTime;
        this.status = "pending";
        this.createdAt = System.currentTimeMillis();
    }

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

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status != null ? status : "pending";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
