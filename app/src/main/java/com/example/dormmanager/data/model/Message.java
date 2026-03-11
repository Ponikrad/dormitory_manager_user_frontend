package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Message implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("senderId")
    private Long senderId;

    @SerializedName("senderName")
    private String senderName;

    @SerializedName("recipientId")
    private Long recipientId;

    @SerializedName("recipientName")
    private String recipientName;

    @SerializedName("subject")
    private String subject;

    @SerializedName("content")
    private String content;

    @SerializedName("messageType")
    private String messageType; // DIRECT, INQUIRY, COMPLAINT, REQUEST, etc.

    @SerializedName("status")
    private String status; // SENT, DELIVERED, READ, REPLIED, RESOLVED

    @SerializedName("isFromAdmin")
    private Boolean isFromAdmin;

    @SerializedName("recipientDepartment")
    private String recipientDepartment;

    @SerializedName("threadId")
    private String threadId;

    @SerializedName("parentMessageId")
    private Long parentMessageId;

    @SerializedName("priority")
    private Integer priority;

    @SerializedName("isUrgent")
    private Boolean isUrgent;

    @SerializedName("requiresResponse")
    private Boolean requiresResponse;

    @SerializedName("sentAt")
    private String sentAt;

    @SerializedName("readAt")
    private String readAt;

    @SerializedName("repliedAt")
    private String repliedAt;

    @SerializedName("resolvedAt")
    private String resolvedAt;

    @SerializedName("attachments")
    private String attachments;

    public Message() {}

    public Long getId() { return id; }
    public Long getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public Long getRecipientId() { return recipientId; }
    public String getRecipientName() { return recipientName; }
    public String getSubject() { return subject; }
    public String getContent() { return content; }
    public String getMessageType() { return messageType; }
    public String getStatus() { return status; }
    public Boolean getIsFromAdmin() { return isFromAdmin; }
    public String getRecipientDepartment() { return recipientDepartment; }
    public String getThreadId() { return threadId; }
    public Long getParentMessageId() { return parentMessageId; }
    public Integer getPriority() { return priority; }
    public Boolean getIsUrgent() { return isUrgent; }
    public Boolean getRequiresResponse() { return requiresResponse; }
    public String getSentAt() { return sentAt; }
    public String getReadAt() { return readAt; }
    public String getRepliedAt() { return repliedAt; }
    public String getResolvedAt() { return resolvedAt; }
    public String getAttachments() { return attachments; }

    public void setId(Long id) { this.id = id; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setContent(String content) { this.content = content; }
    public void setStatus(String status) { this.status = status; }

    public boolean isFromAdmin() {
        return Boolean.TRUE.equals(isFromAdmin);
    }

    public boolean isRead() {
        return readAt != null || "READ".equalsIgnoreCase(status) ||
                "REPLIED".equalsIgnoreCase(status) || "RESOLVED".equalsIgnoreCase(status);
    }

    public boolean isUnread() {
        return !isRead();
    }

    public boolean needsResponse() {
        return Boolean.TRUE.equals(requiresResponse) &&
                !"REPLIED".equalsIgnoreCase(status) &&
                !"RESOLVED".equalsIgnoreCase(status);
    }

    public boolean isUrgent() {
        return Boolean.TRUE.equals(isUrgent);
    }

    public String getMessageIcon() {
        if (messageType == null) return "💬";

        switch (messageType.toUpperCase()) {
            case "INQUIRY":
                return "❓";
            case "COMPLAINT":
                return "😠";
            case "REQUEST":
                return "📝";
            case "MAINTENANCE":
                return "🔧";
            case "PAYMENT":
                return "💰";
            case "RESERVATION":
                return "📅";
            case "REPLY":
                return "↩️";
            default:
                return "💬";
        }
    }

    public String getStatusDisplay() {
        if (status == null) return "Sent";

        switch (status.toUpperCase()) {
            case "SENT":
                return "Sent";
            case "DELIVERED":
                return "Delivered";
            case "READ":
                return "Read";
            case "REPLIED":
                return "Replied";
            case "RESOLVED":
                return "Resolved";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", subject='" + subject + '\'' +
                ", messageType='" + messageType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}