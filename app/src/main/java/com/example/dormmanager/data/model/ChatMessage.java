package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {

    @SerializedName("id")
    private Long id;

    @SerializedName("senderId")
    private Long senderId;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("senderRoomNumber")
    private String senderRoomNumber;

    @SerializedName("content")
    private String content;

    @SerializedName("sentAt")
    private String sentAt;

    @SerializedName("isFromCurrentUser")
    private Boolean isFromCurrentUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderRoomNumber() {
        return senderRoomNumber;
    }

    public void setSenderRoomNumber(String senderRoomNumber) {
        this.senderRoomNumber = senderRoomNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSentAt() {
        return sentAt;
    }

    public void setSentAt(String sentAt) {
        this.sentAt = sentAt;
    }

    public Boolean getIsFromCurrentUser() {
        return isFromCurrentUser;
    }

    public void setIsFromCurrentUser(Boolean fromCurrentUser) {
        isFromCurrentUser = fromCurrentUser;
    }

    public boolean isFromCurrentUser() {
        return Boolean.TRUE.equals(isFromCurrentUser);
    }

    public String getDisplayName() {
        if (senderName == null || senderName.isEmpty()) {
            return "Unknown User";
        }
        if (senderRoomNumber != null && !senderRoomNumber.isEmpty()) {
            return senderName + " (Room " + senderRoomNumber + ")";
        }
        return senderName;
    }
}

