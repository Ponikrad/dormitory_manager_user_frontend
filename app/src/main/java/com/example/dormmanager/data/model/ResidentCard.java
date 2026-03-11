package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ResidentCard implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("username")
    private String username;

    @SerializedName("userEmail")
    private String userEmail;

    @SerializedName("roomNumber")
    private String roomNumber;

    @SerializedName("qrCode")
    private String qrCode;

    @SerializedName("cardNumber")
    private String cardNumber;

    @SerializedName("accessLevel")
    private String accessLevel;

    @SerializedName("issuedDate")
    private String issuedDate;

    @SerializedName("expirationDate")
    private String expirationDate;

    @SerializedName("active")
    private boolean isActive;

    @SerializedName("lastUsed")
    private String lastUsed;

    @SerializedName("usageCount")
    private Long usageCount;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("status")
    private String status;

    @SerializedName("daysUntilExpiration")
    private Long daysUntilExpiration;

    @SerializedName("expired")
    private boolean expired;

    @SerializedName("expiringSoon")
    private boolean expiringSoon;

    public ResidentCard() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getAccessLevel() { return accessLevel; }
    public void setAccessLevel(String accessLevel) { this.accessLevel = accessLevel; }

    public String getIssuedDate() { return issuedDate; }
    public void setIssuedDate(String issuedDate) { this.issuedDate = issuedDate; }

    public String getExpirationDate() { return expirationDate; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getLastUsed() { return lastUsed; }
    public void setLastUsed(String lastUsed) { this.lastUsed = lastUsed; }

    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getDaysUntilExpiration() { return daysUntilExpiration; }
    public void setDaysUntilExpiration(Long daysUntilExpiration) { this.daysUntilExpiration = daysUntilExpiration; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public boolean isExpiringSoon() { return expiringSoon; }
    public void setExpiringSoon(boolean expiringSoon) { this.expiringSoon = expiringSoon; }

    public boolean canAccess() {
        return isActive && !expired;
    }

    public String getDisplayStatus() {
        if (!isActive) return "Inactive";
        if (expired) return "Expired";
        if (expiringSoon) return "Expiring Soon";
        return "Active";
    }

    public String getExpirationInfo() {
        if (expired) return "Expired";
        if (daysUntilExpiration == null) return "No expiration info";
        if (daysUntilExpiration == 0) return "Expires today";
        if (daysUntilExpiration == 1) return "Expires tomorrow";
        return "Expires in " + daysUntilExpiration + " days";
    }

    public int getStatusColor() {
        if (!isActive) return android.R.color.darker_gray;
        if (expired) return android.R.color.holo_red_dark;
        if (expiringSoon) return android.R.color.holo_orange_dark;
        return android.R.color.holo_green_dark;
    }
}