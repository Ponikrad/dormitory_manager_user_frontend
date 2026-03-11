package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class ReservableResource implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("resourceType")
    private String resourceType;

    @SerializedName("location")
    private String location;

    @SerializedName("floorNumber")
    private Integer floorNumber;

    @SerializedName("roomNumber")
    private String roomNumber;

    @SerializedName("capacity")
    private Integer capacity;

    @SerializedName("isActive")
    private Boolean isActive;

    // Availability
    @SerializedName("availableFrom")
    private String availableFrom; // HH:mm

    @SerializedName("availableTo")
    private String availableTo;

    @SerializedName("availableDays")
    private String availableDays;

    @SerializedName("minReservationDuration")
    private Integer minReservationDuration; // minutes

    @SerializedName("maxReservationDuration")
    private Integer maxReservationDuration;

    @SerializedName("reservationInterval")
    private Integer reservationInterval;

    @SerializedName("advanceBookingHours")
    private Integer advanceBookingHours;

    @SerializedName("maxAdvanceDays")
    private Integer maxAdvanceDays;

    @SerializedName("requiresApproval")
    private Boolean requiresApproval;

    @SerializedName("costPerHour")
    private BigDecimal costPerHour;

    @SerializedName("depositRequired")
    private BigDecimal depositRequired;

    @SerializedName("requiresKey")
    private Boolean requiresKey;

    @SerializedName("keyLocation")
    private String keyLocation;

    @SerializedName("keyInstructions")
    private String keyInstructions;

    @SerializedName("maxReservationsPerUserPerDay")
    private Integer maxReservationsPerUserPerDay;

    @SerializedName("maxDurationPerUserPerDay")
    private Integer maxDurationPerUserPerDay;

    @SerializedName("cooldownPeriod")
    private Integer cooldownPeriod;

    @SerializedName("equipment")
    private String equipment;

    @SerializedName("amenities")
    private String amenities;

    @SerializedName("rules")
    private String rules;

    @SerializedName("contactInfo")
    private String contactInfo;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    public ReservableResource() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getResourceType() { return resourceType; }
    public String getLocation() { return location; }
    public Integer getFloorNumber() { return floorNumber; }
    public String getRoomNumber() { return roomNumber; }
    public Integer getCapacity() { return capacity; }
    public Boolean getIsActive() { return isActive; }
    public String getAvailableFrom() { return availableFrom; }
    public String getAvailableTo() { return availableTo; }
    public String getAvailableDays() { return availableDays; }
    public Integer getMinReservationDuration() { return minReservationDuration; }
    public Integer getMaxReservationDuration() { return maxReservationDuration; }
    public Integer getReservationInterval() { return reservationInterval; }
    public Integer getAdvanceBookingHours() { return advanceBookingHours; }
    public Integer getMaxAdvanceDays() { return maxAdvanceDays; }
    public Boolean getRequiresApproval() { return requiresApproval; }
    public BigDecimal getCostPerHour() { return costPerHour; }
    public BigDecimal getDepositRequired() { return depositRequired; }
    public Boolean getRequiresKey() { return requiresKey; }
    public String getKeyLocation() { return keyLocation; }
    public String getKeyInstructions() { return keyInstructions; }
    public Integer getMaxReservationsPerUserPerDay() { return maxReservationsPerUserPerDay; }
    public Integer getMaxDurationPerUserPerDay() { return maxDurationPerUserPerDay; }
    public Integer getCooldownPeriod() { return cooldownPeriod; }
    public String getEquipment() { return equipment; }
    public String getAmenities() { return amenities; }
    public String getRules() { return rules; }
    public String getContactInfo() { return contactInfo; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public void setLocation(String location) { this.location = location; }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isFree() {
        return costPerHour != null && costPerHour.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean requiresKey() {
        return Boolean.TRUE.equals(requiresKey);
    }

    public boolean requiresApproval() {
        return Boolean.TRUE.equals(requiresApproval);
    }

    public String getDisplayLocation() {
        StringBuilder loc = new StringBuilder();

        if (floorNumber != null) {
            loc.append("Floor ").append(floorNumber);
        }

        if (roomNumber != null) {
            if (loc.length() > 0) loc.append(", ");
            loc.append("Room ").append(roomNumber);
        }

        if (location != null && !location.trim().isEmpty()) {
            if (loc.length() > 0) loc.append(", ");
            loc.append(location);
        }

        return loc.toString();
    }

    public String getResourceIcon() {
        if (resourceType == null) return "🏢";

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
            case "RECREATION_ROOM":
                return "🎉";
            case "STORAGE":
                return "📦";
            case "PARKING":
                return "🚗";
            default:
                return "🏢";
        }
    }

    public String getResourceTypeDisplay() {
        if (resourceType == null) return "Other";

        switch (resourceType.toUpperCase()) {
            case "LAUNDRY":
                return "Laundry Room";
            case "GAME_ROOM":
                return "Game Room";
            case "STUDY_ROOM":
                return "Study Room";
            case "KITCHEN":
                return "Kitchen";
            case "GYM":
                return "Gym";
            case "CONFERENCE_ROOM":
                return "Conference Room";
            case "RECREATION_ROOM":
                return "Recreation Room";
            case "STORAGE":
                return "Storage";
            case "PARKING":
                return "Parking";
            default:
                return resourceType;
        }
    }

    @Override
    public String toString() {
        return "ReservableResource{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}