package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Reservation implements Serializable {

    @SerializedName("id")
    private Long id;


    @SerializedName("resourceId")
    private Long resourceId;

    @SerializedName("resourceName")
    private String resourceName;

    @SerializedName("resourceType")
    private String resourceType;

    @SerializedName("resourceLocation")
    private String resourceLocation;

    @SerializedName("keyLocation")
    private String keyLocation;

    @SerializedName("requiresKey")
    private Boolean requiresKey;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("endTime")
    private String endTime;

    @SerializedName("status")
    private String status;

    @SerializedName("numberOfPeople")
    private Integer numberOfPeople;

    @SerializedName("notes")
    private String notes;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("keyPickedUp")
    private Boolean keyPickedUp;

    @SerializedName("keyPickedUpAt")
    private String keyPickedUpAt;

    @SerializedName("keyPickedUpBy")
    private String keyPickedUpBy;

    @SerializedName("keyReturned")
    private Boolean keyReturned;

    @SerializedName("keyReturnedAt")
    private String keyReturnedAt;


    @SerializedName("durationMinutes")
    private Integer durationMinutes;

    @SerializedName("formattedDuration")
    private String formattedDuration;

    @SerializedName("active")
    private Boolean active;

    @SerializedName("upcoming")
    private Boolean upcoming;

    @SerializedName("canCancel")
    private Boolean canCancel;

    @SerializedName("canCheckIn")
    private Boolean canCheckIn;

    @SerializedName("canPickUpKey")
    private Boolean canPickUpKey;

    @SerializedName("minutesUntilStart")
    private Long minutesUntilStart;

    @SerializedName("statusDisplay")
    private String statusDisplay;

    public Reservation() {}

    public Long getId() { return id; }
    public Long getResourceId() { return resourceId; }
    public String getResourceName() { return resourceName; }
    public String getResourceType() { return resourceType; }
    public String getResourceLocation() { return resourceLocation; }
    public String getKeyLocation() { return keyLocation; }
    public Boolean getRequiresKey() { return requiresKey; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public Integer getNumberOfPeople() { return numberOfPeople; }
    public String getNotes() { return notes; }
    public String getCreatedAt() { return createdAt; }
    public Boolean getKeyPickedUp() { return keyPickedUp; }
    public String getKeyPickedUpAt() { return keyPickedUpAt; }
    public String getKeyPickedUpBy() { return keyPickedUpBy; }
    public Boolean getKeyReturned() { return keyReturned; }
    public String getKeyReturnedAt() { return keyReturnedAt; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getFormattedDuration() { return formattedDuration; }
    public Boolean getActive() { return active; }
    public Boolean getUpcoming() { return upcoming; }
    public Boolean getCanCancel() { return canCancel; }
    public Boolean getCanCheckIn() { return canCheckIn; }
    public Boolean getCanPickUpKey() { return canPickUpKey; }
    public Long getMinutesUntilStart() { return minutesUntilStart; }
    public String getStatusDisplay() { return statusDisplay; }

    public void setId(Long id) { this.id = id; }
    public void setStatus(String status) { this.status = status; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public boolean isUpcoming() {
        return Boolean.TRUE.equals(upcoming);
    }

    public boolean canCancel() {
        return Boolean.TRUE.equals(canCancel);
    }

    public boolean canCheckIn() {
        return Boolean.TRUE.equals(canCheckIn);
    }

    public boolean requiresKey() {
        return Boolean.TRUE.equals(requiresKey);
    }


    public String getReservationIcon() {
        if (resourceType == null) return "📅";

        switch (resourceType.toUpperCase()) {
            case "LAUNDRY":
                return "🧺";
            case "GAME_ROOM":
                return "🎮";
            case "STUDY_ROOM":
                return "📚";
            case "KITCHEN":
                return "🍳";
            case "GYM":
                return "💪";
            case "CONFERENCE_ROOM":
                return "🏢";
            default:
                return "📅";
        }
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", resourceName='" + resourceName + '\'' +
                ", status='" + status + '\'' +
                ", startTime='" + startTime + '\'' +
                '}';
    }
}