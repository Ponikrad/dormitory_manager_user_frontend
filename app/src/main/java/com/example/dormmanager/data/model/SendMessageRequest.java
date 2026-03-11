package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class SendMessageRequest implements Serializable {

    @SerializedName("subject")
    private String subject;

    @SerializedName("content")
    private String content;

    @SerializedName("type")
    private String type;

    public SendMessageRequest() {}

    public SendMessageRequest(String subject, String content, String type) {
        this.subject = subject;
        this.content = content;
        this.type = type;
    }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getValidationError() {
        if (subject == null || subject.trim().isEmpty()) {
            return "Subject is required";
        }
        if (subject.length() < 3) {
            return "Subject must be at least 3 characters";
        }
        if (subject.length() > 200) {
            return "Subject cannot exceed 200 characters";
        }
        if (content == null || content.trim().isEmpty()) {
            return "Message content is required";
        }
        if (content.length() < 10) {
            return "Message must be at least 10 characters";
        }
        if (content.length() > 2000) {
            return "Message cannot exceed 2000 characters";
        }
        return null;
    }

    @Override
    public String toString() {
        return "SendMessageRequest{" +
                "subject='" + subject + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}