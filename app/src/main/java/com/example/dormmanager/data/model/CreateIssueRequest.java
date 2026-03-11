package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;

public class CreateIssueRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("category")
    private String category;

    @SerializedName("priority")
    private String priority;

    @SerializedName("locationDetails")
    private String locationDetails;

    public CreateIssueRequest() {}

    public CreateIssueRequest(String title, String description, String category) {
        this.title = title;
        this.description = description;
        this.category = category;
    }

    public CreateIssueRequest(String title, String description, String category, String priority) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
    }

    public CreateIssueRequest(String title, String description, String category, String priority, String locationDetails) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.locationDetails = locationDetails;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getLocationDetails() { return locationDetails; }
    public void setLocationDetails(String locationDetails) { this.locationDetails = locationDetails; }

    public boolean isValid() {
        return title != null && !title.trim().isEmpty() &&
                title.length() >= 5 && title.length() <= 100 &&
                description != null && !description.trim().isEmpty() &&
                description.length() >= 10 && description.length() <= 1000 &&
                category != null && !category.trim().isEmpty();
    }

    public String getValidationError() {
        if (title == null || title.trim().isEmpty()) {
            return "Title is required";
        }
        if (title.length() < 5 || title.length() > 100) {
            return "Title must be between 5 and 100 characters";
        }
        if (description == null || description.trim().isEmpty()) {
            return "Description is required";
        }
        if (description.length() < 10 || description.length() > 1000) {
            return "Description must be between 10 and 1000 characters";
        }
        if (category == null || category.trim().isEmpty()) {
            return "Category is required";
        }
        if (locationDetails != null && locationDetails.length() > 100) {
            return "Location details cannot exceed 100 characters";
        }
        return null;
    }

    @Override
    public String toString() {
        return "CreateIssueRequest{" +
                "title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", priority='" + priority + '\'' +
                ", locationDetails='" + locationDetails + '\'' +
                '}';
    }
}