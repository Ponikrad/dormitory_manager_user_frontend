package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CreateReservationRequest implements Serializable {

    @SerializedName("resourceId")
    private Long resourceId;

    @SerializedName("startTime")
    private String startTime; // Format: "yyyy-MM-dd HH:mm:ss"

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("numberOfPeople")
    private Integer numberOfPeople = 1;

    @SerializedName("notes")
    private String notes;

    public CreateReservationRequest() {}

    public CreateReservationRequest(Long resourceId, String startTime, String endTime) {
        this.resourceId = resourceId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.numberOfPeople = 1;
    }

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Integer getNumberOfPeople() { return numberOfPeople; }
    public void setNumberOfPeople(Integer numberOfPeople) { this.numberOfPeople = numberOfPeople; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getValidationError() {
        if (resourceId == null) {
            return "Please select a resource";
        }
        if (startTime == null || startTime.trim().isEmpty()) {
            return "Start time is required";
        }
        if (endTime == null || endTime.trim().isEmpty()) {
            return "End time is required";
        }
        if (numberOfPeople == null || numberOfPeople < 1) {
            return "Number of people must be at least 1";
        }
        if (numberOfPeople > 50) {
            return "Number of people cannot exceed 50";
        }
        if (notes != null && notes.length() > 500) {
            return "Notes cannot exceed 500 characters";
        }
        return null;
    }

    @Override
    public String toString() {
        return "CreateReservationRequest{" +
                "resourceId=" + resourceId +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", numberOfPeople=" + numberOfPeople +
                '}';
    }
}