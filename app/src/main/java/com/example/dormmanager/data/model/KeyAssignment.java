package com.example.dormmanager.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.math.BigDecimal;

public class KeyAssignment implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("keyId")
    private Long keyId;

    @SerializedName("keyCode")
    private String keyCode;

    @SerializedName("keyDescription")
    private String keyDescription;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("userName")
    private String userName;

    @SerializedName("assignmentType")
    private String assignmentType;

    @SerializedName("status")
    private String status;
    @SerializedName("issuedAt")
    private String issuedAt;

    @SerializedName("expectedReturn")
    private String expectedReturn;

    @SerializedName("returnedAt")
    private String returnedAt;

    @SerializedName("depositAmount")
    private BigDecimal depositAmount;

    @SerializedName("depositPaid")
    private Boolean depositPaid;

    @SerializedName("fineAmount")
    private BigDecimal fineAmount;

    @SerializedName("replacementCost")
    private BigDecimal replacementCost;

    @SerializedName("issueNotes")
    private String issueNotes;

    @SerializedName("returnNotes")
    private String returnNotes;

    @SerializedName("conditionOnIssue")
    private String conditionOnIssue;

    @SerializedName("conditionOnReturn")
    private String conditionOnReturn;

    @SerializedName("issuedByName")
    private String issuedByName;

    @SerializedName("returnedToName")
    private String returnedToName;

    @SerializedName("active")
    private Boolean active;

    @SerializedName("overdueNow")
    private Boolean overdueNow;

    @SerializedName("hoursOverdue")
    private Long hoursOverdue;

    @SerializedName("daysOverdue")
    private Long daysOverdue;

    @SerializedName("totalAmountOwed")
    private BigDecimal totalAmountOwed;

    @SerializedName("assignmentSummary")
    private String assignmentSummary;

    public KeyAssignment() {}

    public Long getId() { return id; }
    public Long getKeyId() { return keyId; }
    public String getKeyCode() { return keyCode; }
    public String getKeyDescription() { return keyDescription; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getAssignmentType() { return assignmentType; }
    public String getStatus() { return status; }
    public String getIssuedAt() { return issuedAt; }
    public String getExpectedReturn() { return expectedReturn; }
    public String getReturnedAt() { return returnedAt; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public Boolean getDepositPaid() { return depositPaid; }
    public BigDecimal getFineAmount() { return fineAmount; }
    public BigDecimal getReplacementCost() { return replacementCost; }
    public String getIssueNotes() { return issueNotes; }
    public String getReturnNotes() { return returnNotes; }
    public String getConditionOnIssue() { return conditionOnIssue; }
    public String getConditionOnReturn() { return conditionOnReturn; }
    public String getIssuedByName() { return issuedByName; }
    public String getReturnedToName() { return returnedToName; }
    public Boolean getActive() { return active; }
    public Boolean getOverdueNow() { return overdueNow; }
    public Long getHoursOverdue() { return hoursOverdue; }
    public Long getDaysOverdue() { return daysOverdue; }
    public BigDecimal getTotalAmountOwed() { return totalAmountOwed; }
    public String getAssignmentSummary() { return assignmentSummary; }

    public void setId(Long id) { this.id = id; }
    public void setKeyCode(String keyCode) { this.keyCode = keyCode; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public boolean isOverdue() {
        return Boolean.TRUE.equals(overdueNow);
    }

    public boolean isReturned() {
        return "RETURNED".equalsIgnoreCase(status);
    }

    public boolean isLost() {
        return "LOST".equalsIgnoreCase(status);
    }

    public boolean isPermanent() {
        return "PERMANENT".equalsIgnoreCase(assignmentType);
    }

    public boolean isTemporary() {
        return "TEMPORARY".equalsIgnoreCase(assignmentType);
    }

    public String getAssignmentTypeDisplay() {
        if (assignmentType == null) return "Unknown";

        switch (assignmentType.toUpperCase()) {
            case "PERMANENT":
                return "Permanent";
            case "TEMPORARY":
                return "Temporary";
            case "EMERGENCY":
                return "Emergency";
            default:
                return assignmentType;
        }
    }

    public String getStatusDisplay() {
        if (status == null) return "Unknown";

        switch (status.toUpperCase()) {
            case "ACTIVE":
                return "Active";
            case "RETURNED":
                return "Returned";
            case "LOST":
                return "Lost";
            case "CANCELLED":
                return "Cancelled";
            default:
                return status;
        }
    }

    public int getStatusColor() {
        if (status == null) return android.R.color.darker_gray;

        switch (status.toUpperCase()) {
            case "ACTIVE":
                return android.R.color.holo_green_dark;
            case "RETURNED":
                return android.R.color.holo_blue_dark;
            case "LOST":
                return android.R.color.holo_red_dark;
            case "CANCELLED":
                return android.R.color.darker_gray;
            default:
                return android.R.color.darker_gray;
        }
    }

    public String getKeyIcon() {
        if (isLost()) return "🔴";
        if (isOverdue()) return "⚠️";
        if (isReturned()) return "✅";
        if (isActive()) return "🔑";
        return "🗝️";
    }

    @Override
    public String toString() {
        return "KeyAssignment{" +
                "id=" + id +
                ", keyCode='" + keyCode + '\'' +
                ", status='" + status + '\'' +
                ", assignmentType='" + assignmentType + '\'' +
                '}';
    }
}