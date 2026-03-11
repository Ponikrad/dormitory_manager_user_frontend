package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Issue implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("userFullName")
    private String userFullName;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category; // PLUMBING, ELECTRICAL, FURNITURE, CLEANING...

    @SerializedName("status")
    private String status; // REPORTED, ACKNOWLEDGED, IN_PROGRESS, RESOLVED, CANCELLED

    @SerializedName("priority")
    private String priority; // LOW, MEDIUM, HIGH, URGENT, CRITICAL

    @SerializedName("roomNumber")
    private String roomNumber;

    @SerializedName("locationDetails")
    private String locationDetails;

    @SerializedName("reportedAt")
    private String reportedAt;


    @SerializedName("adminNotes")
    private String adminNotes;

    @SerializedName("assignedToUserName")
    private String assignedToUserName;

    @SerializedName("userSatisfactionRating")
    private Integer userSatisfactionRating;

    @SerializedName("overdue")
    private boolean overdue;

    @SerializedName("statusDisplay")
    private String statusDisplay;

    @SerializedName("priorityDisplay")
    private String priorityDisplay;

    @SerializedName("categoryDisplay")
    private String categoryDisplay;

    public Issue() {}

    public Issue(String title, String description, String category) {
        this.title = title;
        this.description = description;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getLocationDetails() { return locationDetails; }
    public void setLocationDetails(String locationDetails) { this.locationDetails = locationDetails; }

    public String getReportedAt() { return reportedAt; }
    public void setReportedAt(String reportedAt) { this.reportedAt = reportedAt; }


    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }

    public String getAssignedToUserName() { return assignedToUserName; }
    public void setAssignedToUserName(String assignedToUserName) { this.assignedToUserName = assignedToUserName; }

    public Integer getUserSatisfactionRating() { return userSatisfactionRating; }
    public void setUserSatisfactionRating(Integer userSatisfactionRating) { this.userSatisfactionRating = userSatisfactionRating; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public String getPriorityDisplay() { return priorityDisplay; }
    public void setPriorityDisplay(String priorityDisplay) { this.priorityDisplay = priorityDisplay; }

    public String getCategoryDisplay() { return categoryDisplay; }
    public void setCategoryDisplay(String categoryDisplay) { this.categoryDisplay = categoryDisplay; }

    public boolean isOpen() {
        return status != null && !status.equals("RESOLVED") && !status.equals("CANCELLED") && !status.equals("CLOSED");
    }

    public boolean isResolved() {
        return "RESOLVED".equals(status);
    }

    public Boolean canBeReopened() {
        return status != null &&
                (status.equals("RESOLVED") || status.equals("CANCELLED") || status.equals("CLOSED"));
    }


    public String getIssueIcon() {
        if (category == null) return "🔧";
        switch (category.toUpperCase()) {
            case "PLUMBING":
                return "🚰";
            case "ELECTRICAL":
                return "⚡";
            case "FURNITURE":
                return "🪑";
            case "CLEANING":
                return "🧹";
            case "INTERNET":
                return "📶";
            case "SECURITY":
                return "🔒";
            case "HEATING":
                return "🌡️";
            case "KITCHEN":
                return "🍽️";
            case "BATHROOM":
                return "🚿";
            case "NOISE":
                return "🔊";
            default:
                return "🔧";
        }
    }

    public int getStatusColor() {
        if (status == null) return android.R.color.darker_gray;
        switch (status.toUpperCase()) {
            case "RESOLVED":
                return android.R.color.holo_green_dark;
            case "IN_PROGRESS":
            case "ACKNOWLEDGED":
                return android.R.color.holo_blue_dark;
            case "REPORTED":
                return android.R.color.holo_orange_dark;
            case "CANCELLED":
            case "CLOSED":
                return android.R.color.holo_red_dark;
            default:
                return android.R.color.darker_gray;
        }
    }

    public int getPriorityColor() {
        if (priority == null) return android.R.color.darker_gray;
        switch (priority.toUpperCase()) {
            case "CRITICAL":
            case "URGENT":
                return android.R.color.holo_red_dark;
            case "HIGH":
                return android.R.color.holo_orange_dark;
            case "MEDIUM":
                return android.R.color.holo_blue_dark;
            case "LOW":
                return android.R.color.holo_green_dark;
            default:
                return android.R.color.darker_gray;
        }
    }

    @Override
    public String toString() {
        return "Issue{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", reportedAt='" + reportedAt + '\'' +
                '}';
    }
}