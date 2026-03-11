package com.example.dormmanager.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {

    public static final String API_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String API_FORMAT_WITH_T = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "MMM dd, yyyy";
    public static final String DISPLAY_DATETIME_FORMAT = "MMM dd, yyyy HH:mm";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "Unknown";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(API_FORMAT_WITH_T, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());

            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            try {
                SimpleDateFormat inputFormat2 = new SimpleDateFormat(API_FORMAT, Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());

                Date date = inputFormat2.parse(dateStr);
                return outputFormat.format(date);
            } catch (ParseException e2) {
                return dateStr.substring(0, Math.min(10, dateStr.length()));
            }
        }
    }

    public static String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "Unknown";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(API_FORMAT_WITH_T, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATETIME_FORMAT, Locale.getDefault());

            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            // Try alternative format
            try {
                SimpleDateFormat inputFormat2 = new SimpleDateFormat(API_FORMAT, Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat(DISPLAY_DATETIME_FORMAT, Locale.getDefault());

                Date date = inputFormat2.parse(dateTimeStr);
                return outputFormat.format(date);
            } catch (ParseException e2) {
                return dateTimeStr.substring(0, Math.min(16, dateTimeStr.length()));
            }
        }
    }

    public static String formatTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "Unknown";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(API_FORMAT_WITH_T, Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());

            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateTimeStr.substring(11, Math.min(16, dateTimeStr.length()));
        }
    }

    public static String calendarToApiFormat(Calendar calendar) {
        SimpleDateFormat format = new SimpleDateFormat(API_FORMAT, Locale.getDefault());
        return format.format(calendar.getTime());
    }

    public static String getRelativeTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "Unknown";

        try {
            SimpleDateFormat format = new SimpleDateFormat(API_FORMAT_WITH_T, Locale.getDefault());
            Date date = format.parse(dateTimeStr);

            long diff = new Date().getTime() - date.getTime();
            long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long diffHours = TimeUnit.MILLISECONDS.toHours(diff);
            long diffDays = TimeUnit.MILLISECONDS.toDays(diff);

            if (diffMinutes < 1) {
                return "just now";
            } else if (diffMinutes < 60) {
                return diffMinutes + " minutes ago";
            } else if (diffHours < 24) {
                return diffHours + " hours ago";
            } else if (diffDays < 7) {
                return diffDays + " days ago";
            } else {
                return formatDate(dateTimeStr);
            }
        } catch (ParseException e) {
            return dateTimeStr;
        }
    }

    public static String getTimeUntil(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return "Unknown";

        try {
            SimpleDateFormat format = new SimpleDateFormat(API_FORMAT_WITH_T, Locale.getDefault());
            Date date = format.parse(dateTimeStr);

            long diff = date.getTime() - new Date().getTime();

            if (diff < 0) return "passed";

            long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long diffHours = TimeUnit.MILLISECONDS.toHours(diff);
            long diffDays = TimeUnit.MILLISECONDS.toDays(diff);

            if (diffMinutes < 1) {
                return "now";
            } else if (diffMinutes < 60) {
                return "in " + diffMinutes + " minutes";
            } else if (diffHours < 24) {
                return "in " + diffHours + " hours";
            } else {
                return "in " + diffDays + " days";
            }
        } catch (ParseException e) {
            return dateTimeStr;
        }
    }

    public static boolean isToday(String dateStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(API_FORMAT_WITH_T, Locale.getDefault());
            Date date = format.parse(dateStr);

            Calendar today = Calendar.getInstance();
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(date);

            return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR);
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isFuture(String dateTimeStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(API_FORMAT_WITH_T, Locale.getDefault());
            Date date = format.parse(dateTimeStr);
            return date.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isPast(String dateTimeStr) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(API_FORMAT_WITH_T, Locale.getDefault());
            Date date = format.parse(dateTimeStr);
            return date.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    public static long getDurationMinutes(String startDateTime, String endDateTime) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(API_FORMAT_WITH_T, Locale.getDefault());
            Date start = format.parse(startDateTime);
            Date end = format.parse(endDateTime);

            long diff = end.getTime() - start.getTime();
            return TimeUnit.MILLISECONDS.toMinutes(diff);
        } catch (ParseException e) {
            return 0;
        }
    }

    public static String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + "m";
        }

        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;

        if (remainingMinutes == 0) {
            return hours + "h";
        } else {
            return hours + "h " + remainingMinutes + "m";
        }
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat format = new SimpleDateFormat(API_FORMAT, Locale.getDefault());
        return format.format(new Date());
    }

    public static String addHours(int hours) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendarToApiFormat(calendar);
    }

    public static String addDays(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendarToApiFormat(calendar);
    }
}