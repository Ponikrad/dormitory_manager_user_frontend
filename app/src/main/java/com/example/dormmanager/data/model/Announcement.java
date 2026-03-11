package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Announcement implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("authorId")
    private Long authorId;

    @SerializedName("authorName")
    private String authorName;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("type")
    private String type; // GENERAL, MAINTENANCE, EMERGENCY, EVENT, etc.

    @SerializedName("priority")
    private String priority; // LOW, NORMAL, HIGH, CRITICAL

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("isUrgent")
    private Boolean isUrgent;

    @SerializedName("isPinned")
    private Boolean isPinned;

    @SerializedName("targetAudience")
    private String targetAudience; // ALL, STUDENTS, STAFF

    @SerializedName("publishedAt")
    private String publishedAt;

    @SerializedName("expiresAt")
    private String expiresAt;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("viewCount")
    private Long viewCount;

    @SerializedName("imageUrl")
    private String imageUrl;

    public Announcement() {}

    public Long getId() { return id; }
    public String getAuthorName() { return authorName; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getType() { return type; }
    public String getPriority() { return priority; }
    public Boolean getIsActive() { return isActive; }
    public Boolean getIsUrgent() { return isUrgent; }
    public Boolean getIsPinned() { return isPinned; }
    public String getPublishedAt() { return publishedAt; }
    public String getExpiresAt() { return expiresAt; }
    public String getCreatedAt() { return createdAt; }
    public Long getViewCount() { return viewCount; }
    public String getImageUrl() { return imageUrl; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isUrgent() {
        return Boolean.TRUE.equals(isUrgent);
    }

    public boolean isPinned() {
        return Boolean.TRUE.equals(isPinned);
    }

    public String getAnnouncementIcon() {
        if (type == null) return "📢";

        switch (type.toUpperCase()) {
            case "MAINTENANCE":
                return "🔧";
            case "EMERGENCY":
                return "🚨";
            case "EVENT":
                return "🎉";
            case "RULE_CHANGE":
                return "📋";
            case "FACILITY_UPDATE":
                return "🏢";
            case "PAYMENT_REMINDER":
                return "💰";
            case "NEWS":
                return "📰";
            default:
                return "📢";
        }
    }

    public String getPriorityIcon() {
        if (priority == null) return "";

        switch (priority.toUpperCase()) {
            case "CRITICAL":
                return "🔴";
            case "HIGH":
                return "🟠";
            case "NORMAL":
                return "🟢";
            case "LOW":
                return "🔵";
            default:
                return "";
        }
    }

    public int getPriorityColor() {
        if (priority == null) return android.R.color.darker_gray;

        switch (priority.toUpperCase()) {
            case "CRITICAL":
                return android.R.color.holo_red_dark;
            case "HIGH":
                return android.R.color.holo_orange_dark;
            case "NORMAL":
                return android.R.color.holo_green_dark;
            case "LOW":
                return android.R.color.holo_blue_dark;
            default:
                return android.R.color.darker_gray;
        }
    }

    public String getTypeDisplay() {
        if (type == null) return "General";

        switch (type.toUpperCase()) {
            case "GENERAL":
                return "General";
            case "MAINTENANCE":
                return "Maintenance";
            case "EMERGENCY":
                return "Emergency";
            case "EVENT":
                return "Event";
            case "RULE_CHANGE":
                return "Rule Change";
            case "FACILITY_UPDATE":
                return "Facility Update";
            case "PAYMENT_REMINDER":
                return "Payment Reminder";
            case "NEWS":
                return "News";
            default:
                return type;
        }
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }
}