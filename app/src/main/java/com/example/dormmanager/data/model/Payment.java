package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Payment implements Serializable {
    @SerializedName("id")
    private Long id;


    @SerializedName("amount")
    private Double amount;

    @SerializedName("paymentMethod")
    private String paymentMethod; // CARD, BLIK, BANK_TRANSFER, CASH, ONLINE

    @SerializedName("status")
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED

    @SerializedName("description")
    private String description;

    @SerializedName("paymentType")
    private String paymentType; // RENT, UTILITIES, DEPOSIT, etc.


    @SerializedName("dueDate")
    private String dueDate;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("overdue")
    private boolean overdue;

    @SerializedName("displayAmount")
    private String displayAmount;

    @SerializedName("statusDisplay")
    private String statusDisplay;

    public Payment() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }


    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public boolean isOverdue() { return overdue; }
    public void setOverdue(boolean overdue) { this.overdue = overdue; }

    public String getDisplayAmount() { return displayAmount; }
    public void setDisplayAmount(String displayAmount) { this.displayAmount = displayAmount; }

    public String getStatusDisplay() { return statusDisplay; }
    public void setStatusDisplay(String statusDisplay) { this.statusDisplay = statusDisplay; }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    public String getFormattedAmount() {
        if (displayAmount != null) return displayAmount;
        if (amount != null) {
            return String.format("%.2f PLN", amount);
        }
        return "0.00 PLN";
    }

    public int getStatusColor() {
        if (status == null) return android.R.color.darker_gray;

        switch (status) {
            case "COMPLETED":
                return android.R.color.holo_green_dark;
            case "PENDING":
            case "PROCESSING":
                return android.R.color.holo_orange_dark;
            case "FAILED":
            case "CANCELLED":
                return android.R.color.holo_red_dark;
            default:
                return android.R.color.darker_gray;
        }
    }
}