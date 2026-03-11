package com.example.dormmanager.utils;

import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;

public class ImageValidationHelper {

    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final int MAX_DIMENSION = 4096;
    public static final String[] ALLOWED_MIME_TYPES = {
            "image/jpeg",
            "image/jpg",
            "image/png"
    };
    public static final String[] ALLOWED_EXTENSIONS = {
            ".jpg",
            ".jpeg",
            ".png"
    };

    public static boolean isValidMimeType(String mimeType) {
        if (mimeType == null) return false;

        for (String allowedType : ALLOWED_MIME_TYPES) {
            if (allowedType.equalsIgnoreCase(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValidExtension(String fileName) {
        if (fileName == null) return false;

        String lowerFileName = fileName.toLowerCase();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerFileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static long getFileSize(Context context, Uri uri) {
        try {
            Cursor cursor = context.getContentResolver().query(
                    uri, null, null, null, null);

            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                cursor.moveToFirst();
                long size = cursor.getLong(sizeIndex);
                cursor.close();
                return size;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getFileName(Context context, Uri uri) {
        try {
            Cursor cursor = context.getContentResolver().query(
                    uri, null, null, null, null);

            if (cursor != null) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                String name = cursor.getString(nameIndex);
                cursor.close();
                return name;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    public static boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }

    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }


    public static String getValidationErrorMessage(ValidationError error) {
        switch (error) {
            case INVALID_TYPE:
                return "Invalid file type.\n\nOnly JPEG and PNG images are allowed.";
            case FILE_TOO_LARGE:
                return "File is too large.\n\nMaximum size: 5MB\n\nPlease compress the image or choose a smaller one.";
            case FILE_EMPTY:
                return "The selected file is empty.\n\nPlease choose a valid image.";
            case DIMENSIONS_TOO_LARGE:
                return "Image dimensions are too large.\n\nMaximum: 4096x4096 pixels";
            case FILE_NOT_FOUND:
                return "Image file not found.\n\nPlease try selecting the file again.";
            default:
                return "Unknown validation error occurred.";
        }
    }


    public enum ValidationError {
        INVALID_TYPE,
        FILE_TOO_LARGE,
        FILE_EMPTY,
        DIMENSIONS_TOO_LARGE,
        FILE_NOT_FOUND,
        NONE
    }

    public static class ValidationResult {
        public boolean isValid;
        public ValidationError error;
        public String message;

        public ValidationResult(boolean isValid, ValidationError error, String message) {
            this.isValid = isValid;
            this.error = error;
            this.message = message;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, ValidationError.NONE, "Validation passed");
        }

        public static ValidationResult failure(ValidationError error) {
            return new ValidationResult(false, error, getValidationErrorMessage(error));
        }
    }

    public static ValidationResult validateImage(Context context, Uri uri) {
        // Check if URI is valid
        if (uri == null) {
            return ValidationResult.failure(ValidationError.FILE_NOT_FOUND);
        }

        // Validate MIME type
        String mimeType = context.getContentResolver().getType(uri);
        if (!isValidMimeType(mimeType)) {
            return ValidationResult.failure(ValidationError.INVALID_TYPE);
        }

        // Validate file size
        long fileSize = getFileSize(context, uri);
        if (fileSize == 0) {
            return ValidationResult.failure(ValidationError.FILE_EMPTY);
        }
        if (!isValidFileSize(fileSize)) {
            return ValidationResult.failure(ValidationError.FILE_TOO_LARGE);
        }

        // Validate file extension
        String fileName = getFileName(context, uri);
        if (!isValidExtension(fileName)) {
            return ValidationResult.failure(ValidationError.INVALID_TYPE);
        }

        return ValidationResult.success();
    }
}