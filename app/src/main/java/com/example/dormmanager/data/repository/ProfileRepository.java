package com.example.dormmanager.data.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dormmanager.data.api.ProfileApiService;
import com.example.dormmanager.utils.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {
    private static final String TAG = "ProfileRepository";
    private final ProfileApiService apiService;
    private final Context context;

    public ProfileRepository(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getAuthenticatedClient(context)
                .create(ProfileApiService.class);
    }

    public void uploadProfileImage(File imageFile, ProfileCallback callback) {
        if (!imageFile.exists() || imageFile.length() == 0) {
            callback.onError("Invalid image file");
            return;
        }

        if (imageFile.length() > 5 * 1024 * 1024) {
            callback.onError("File size exceeds maximum limit of 5MB");
            return;
        }

        String fileName = imageFile.getName().toLowerCase();
        String mimeType;
        
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            mimeType = "image/png";
        } else {
            callback.onError("Invalid file type. Only JPEG and PNG images are allowed");
            return;
        }

        Log.d(TAG, "Uploading image: " + fileName + " with MIME type: " + mimeType + ", size: " + imageFile.length() + " bytes");

        RequestBody requestFile = RequestBody.create(
                MediaType.parse(mimeType),
                imageFile
        );

        MultipartBody.Part body = MultipartBody.Part.createFormData(
                "image",
                imageFile.getName(),
                requestFile
        );

        apiService.uploadProfileImage(body).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().get("message") != null
                            ? response.body().get("message").toString()
                            : "Image uploaded successfully";
                    callback.onSuccess(message);
                } else {
                    String errorMessage = parseErrorResponse(response);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error", t);
                String errorMessage = t.getMessage() != null
                        ? "Network error: " + t.getMessage()
                        : "Network error occurred";
                callback.onError(errorMessage);
            }
        });
    }

    private String parseErrorResponse(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();

                // Try to parse JSON error
                if (errorBody.contains("message")) {
                    // Extract message from JSON
                    int start = errorBody.indexOf("\"message\"");
                    if (start != -1) {
                        start = errorBody.indexOf(":", start) + 1;
                        int end = errorBody.indexOf("\"", start + 2);
                        if (end != -1) {
                            String message = errorBody.substring(start + 1, end);
                            return message.replace("\\n", "\n");
                        }
                    }
                }

                return errorBody;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
        }

        switch (response.code()) {
            case 400:
                return "Invalid file type. Only JPEG and PNG images are allowed";
            case 401:
                return "Session expired. Please log in again";
            case 413:
                return "File size exceeds maximum limit of 5MB";
            case 404:
                return "Upload endpoint not found";
            case 500:
                return "Server error occurred";
            default:
                return "Upload failed with code: " + response.code();
        }
    }

    public void getProfileImage(ImageCallback callback) {
        apiService.getProfileImage().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        InputStream inputStream = response.body().byteStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        callback.onSuccess(bitmap);
                    } catch (Exception e) {
                        callback.onError("Failed to decode image");
                    }
                } else if (response.code() == 404) {
                    callback.onImageNotFound();
                } else {
                    callback.onError("Failed to fetch image: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteProfileImage(ProfileCallback callback) {
        apiService.deleteProfileImage().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Image deleted successfully");
                } else {
                    callback.onError("Failed to delete image");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateProfile(String firstName, String lastName, String phoneNumber,
                              String roomNumber, ProfileCallback callback) {
        Map<String, String> updateData = new HashMap<>();
        if (firstName != null && !firstName.isEmpty()) {
            updateData.put("first_name", firstName);
        }
        if (lastName != null && !lastName.isEmpty()) {
            updateData.put("last_name", lastName);
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            updateData.put("phone_number", phoneNumber);
        }
        if (roomNumber != null && !roomNumber.isEmpty()) {
            updateData.put("room_number", roomNumber);
        }

        apiService.updateProfile(updateData).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess("Profile updated successfully");
                } else {
                    callback.onError("Failed to update profile");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void changePassword(String currentPassword, String newPassword,
                               String confirmPassword, ProfileCallback callback) {
        Map<String, String> passwords = new HashMap<>();
        passwords.put("currentPassword", currentPassword);
        passwords.put("newPassword", newPassword);
        passwords.put("confirmPassword", confirmPassword);

        apiService.changePassword(passwords).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call,
                                   @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Password changed successfully");
                } else if (response.code() == 400) {
                    callback.onError("Current password is incorrect");
                } else {
                    callback.onError("Failed to change password");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface ProfileCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ImageCallback {
        void onSuccess(Bitmap bitmap);
        void onImageNotFound();
        void onError(String error);
    }
}