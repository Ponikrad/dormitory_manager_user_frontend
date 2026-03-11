package com.example.dormmanager.utils;

import android.content.Context;
import com.example.dormmanager.R;

public class IssueUtils {

    public static int getStatusColor(Context context, String status) {
        if (status == null) return R.color.status_pending;

        switch (status.toUpperCase()) {
            case "RESOLVED":
                return R.color.status_completed;
            case "IN_PROGRESS":
            case "ACKNOWLEDGED":
                return R.color.status_pending;
            case "REPORTED":
                return R.color.status_pending;
            case "CANCELLED":
            case "CLOSED":
                return R.color.status_failed;
            default:
                return R.color.status_pending;
        }
    }

    public static int getPriorityColor(Context context, String priority) {
        if (priority == null) return R.color.status_pending;

        switch (priority.toUpperCase()) {
            case "CRITICAL":
            case "URGENT":
                return R.color.status_failed;
            case "HIGH":
                return R.color.status_pending;
            case "MEDIUM":
                return R.color.primary_blue_light;
            case "LOW":
                return R.color.status_completed;
            default:
                return R.color.status_pending;
        }
    }

    public static String getStatusDisplayName(String status) {
        if (status == null) return "Unknown";

        switch (status.toUpperCase()) {
            case "REPORTED":
                return "Reported";
            case "ACKNOWLEDGED":
                return "Acknowledged";
            case "IN_PROGRESS":
                return "In Progress";
            case "RESOLVED":
                return "Resolved";
            case "CANCELLED":
                return "Cancelled";
            case "CLOSED":
                return "Closed";
            case "ESCALATED":
                return "Escalated";
            default:
                return status;
        }
    }

    public static String getPriorityDisplayName(String priority) {
        if (priority == null) return "Medium";

        switch (priority.toUpperCase()) {
            case "LOW":
                return "Low";
            case "MEDIUM":
                return "Medium";
            case "HIGH":
                return "High";
            case "URGENT":
                return "Urgent";
            case "CRITICAL":
                return "Critical";
            default:
                return priority;
        }
    }

    public static String getCategoryDisplayName(String category) {
        if (category == null) return "Other";

        switch (category.toUpperCase()) {
            case "PLUMBING":
                return "Plumbing";
            case "ELECTRICAL":
                return "Electrical";
            case "FURNITURE":
                return "Furniture";
            case "CLEANING":
                return "Cleaning & Maintenance";
            case "INTERNET":
                return "Internet & Network";
            case "SECURITY":
                return "Security";
            case "HEATING":
                return "Heating & Cooling";
            case "KITCHEN":
                return "Kitchen Equipment";
            case "BATHROOM":
                return "Bathroom";
            case "NOISE":
                return "Noise Issues";
            case "OTHER":
                return "Other";
            default:
                return category;
        }
    }

    public static String getIssueIcon(String category) {
        if (category == null) return "🔧";

        switch (category.toUpperCase()) {
            case "PLUMBING":
                return "🚰";
            case "ELECTRICAL":
                return "⚡";
            case "FURNITURE":
                return "🪑";
            case "CLEANING":
                return "🧹";
            case "INTERNET":
                return "📶";
            case "SECURITY":
                return "🔒";
            case "HEATING":
                return "🌡️";
            case "KITCHEN":
                return "🍽️";
            case "BATHROOM":
                return "🚿";
            case "NOISE":
                return "🔊";
            default:
                return "🔧";
        }
    }

    public static String getPriorityEmoji(String priority) {
        if (priority == null) return "📌";

        switch (priority.toUpperCase()) {
            case "LOW":
                return "🟢";
            case "MEDIUM":
                return "🟡";
            case "HIGH":
                return "🟠";
            case "URGENT":
            case "CRITICAL":
                return "🔴";
            default:
                return "📌";
        }
    }

    public static boolean isUrgent(String priority) {
        return priority != null &&
                (priority.equals("URGENT") || priority.equals("CRITICAL"));
    }

    public static boolean isResolved(String status) {
        return status != null && status.equals("RESOLVED");
    }

    public static boolean canReopen(String status) {
        return status != null &&
                (status.equals("RESOLVED") || status.equals("CANCELLED") || status.equals("CLOSED"));
    }
}