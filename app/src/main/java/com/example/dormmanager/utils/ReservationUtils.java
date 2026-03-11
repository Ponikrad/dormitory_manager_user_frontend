package com.example.dormmanager.utils;

import android.content.Context;
import com.example.dormmanager.R;

public class ReservationUtils {

    public static int getStatusColor(Context context, String status) {
        if (status == null) return R.color.status_pending;

        switch (status.toUpperCase()) {
            case "CONFIRMED":
                return R.color.status_completed;
            case "CHECKED_IN":
                return R.color.primary_blue;
            case "COMPLETED":
                return R.color.status_completed;
            case "PENDING":
                return R.color.status_pending;
            case "CANCELLED":
            case "REJECTED":
            case "NO_SHOW":
                return R.color.status_failed;
            case "EXPIRED":
                return R.color.status_failed;
            default:
                return R.color.status_pending;
        }
    }

    public static String getStatusDisplayName(String status) {
        if (status == null) return "Unknown";

        switch (status.toUpperCase()) {
            case "PENDING":
                return "Pending";
            case "CONFIRMED":
                return "Confirmed";
            case "CHECKED_IN":
                return "Checked In";
            case "COMPLETED":
                return "Completed";
            case "CANCELLED":
                return "Cancelled";
            case "REJECTED":
                return "Rejected";
            case "NO_SHOW":
                return "No Show";
            case "EXPIRED":
                return "Expired";
            default:
                return status;
        }
    }

    public static String getResourceTypeDisplayName(String resourceType) {
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

    public static String getResourceIcon(String resourceType) {
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

    public static String getStatusIcon(String status) {
        if (status == null) return "📅";

        switch (status.toUpperCase()) {
            case "PENDING":
                return "⏳";
            case "CONFIRMED":
                return "✅";
            case "CHECKED_IN":
                return "🔵";
            case "COMPLETED":
                return "✔️";
            case "CANCELLED":
                return "❌";
            case "REJECTED":
                return "🚫";
            case "NO_SHOW":
                return "⚠️";
            case "EXPIRED":
                return "⏰";
            default:
                return "📅";
        }
    }

    public static boolean canBeCancelled(String status, String startTime) {
        if (status == null) return false;

        // Can only cancel if status is PENDING or CONFIRMED
        if (!status.equalsIgnoreCase("PENDING") &&
                !status.equalsIgnoreCase("CONFIRMED")) {
            return false;
        }

        // Check if start time is at least 2 hours in the future
        if (startTime != null) {
            return DateTimeUtils.isFuture(startTime);
        }

        return true;
    }

    public static boolean canCheckIn(String status, String startTime) {
        if (status == null || !status.equalsIgnoreCase("CONFIRMED")) {
            return false;
        }

        // Can check in 15 minutes before to 30 minutes after start time
        // This is a simplified version - actual logic is in backend
        return startTime != null;
    }

    public static String formatCost(double cost) {
        if (cost == 0) {
            return "Free";
        }
        return String.format("%.2f PLN", cost);
    }

    public static String getKeyStatusMessage(Boolean requiresKey, Boolean keyPickedUp,
                                             Boolean keyReturned, String keyLocation) {
        if (requiresKey == null || !requiresKey) {
            return "No key required";
        }

        if (Boolean.TRUE.equals(keyReturned)) {
            return "🔑 Key returned";
        } else if (Boolean.TRUE.equals(keyPickedUp)) {
            return "🔑 Key picked up - needs return";
        } else {
            return "🔑 Pick up key at " + (keyLocation != null ? keyLocation : "reception");
        }
    }

    public static String validateReservationTime(String startTime, String endTime) {
        if (startTime == null || endTime == null) {
            return "Start and end times are required";
        }

        if (!DateTimeUtils.isFuture(startTime)) {
            return "Start time must be in the future";
        }

        long duration = DateTimeUtils.getDurationMinutes(startTime, endTime);

        if (duration <= 0) {
            return "End time must be after start time";
        }

        if (duration < 30) {
            return "Reservation must be at least 30 minutes";
        }

        if (duration > 480) { // 8 hours
            return "Reservation cannot exceed 8 hours";
        }

        return null; // No error
    }

    public static int getPriorityColor(Context context, String status, String startTime) {
        if ("CHECKED_IN".equalsIgnoreCase(status)) {
            return R.color.primary_blue;
        }

        if (startTime != null && DateTimeUtils.isFuture(startTime)) {
            String timeUntil = DateTimeUtils.getTimeUntil(startTime);
            if (timeUntil.contains("hour") || timeUntil.contains("minute")) {
                return R.color.status_pending; // Orange for upcoming
            }
        }

        return getStatusColor(context, status);
    }

    public static String getNextAction(String status, Boolean canCheckIn, Boolean canPickUpKey,
                                       Boolean needsKeyReturn, String startTime) {
        if (Boolean.TRUE.equals(needsKeyReturn)) {
            return "Return key";
        }

        if (Boolean.TRUE.equals(canPickUpKey)) {
            return "Pick up key";
        }

        if (Boolean.TRUE.equals(canCheckIn)) {
            return "Check in";
        }

        if ("CONFIRMED".equalsIgnoreCase(status) && startTime != null) {
            return "Starts " + DateTimeUtils.getTimeUntil(startTime);
        }

        return getStatusDisplayName(status);
    }


    public static String validatePeopleCount(int numberOfPeople, Integer capacity) {
        if (numberOfPeople < 1) {
            return "At least 1 person required";
        }

        if (numberOfPeople > 50) {
            return "Maximum 50 people allowed";
        }

        if (capacity != null && numberOfPeople > capacity) {
            return "Exceeds capacity of " + capacity;
        }

        return null; // No error
    }
}