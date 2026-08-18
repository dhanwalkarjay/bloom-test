package com.bloom.customer.data.model;

import com.google.gson.annotations.SerializedName;

public class Occasion {
    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("title")
    private String title;

    @SerializedName("target_date")
    private String targetDate; // YYYY-MM-DD format

    @SerializedName("recipient_name")
    private String recipientName;

    @SerializedName("recipient_relation")
    private String recipientRelation;

    @SerializedName("created_at")
    private String createdAt;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getRecipientRelation() { return recipientRelation; }
    public void setRecipientRelation(String recipientRelation) { this.recipientRelation = recipientRelation; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
