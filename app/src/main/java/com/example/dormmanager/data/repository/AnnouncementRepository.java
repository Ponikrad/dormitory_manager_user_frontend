package com.example.dormmanager.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dormmanager.data.api.AnnouncementApiService;
import com.example.dormmanager.data.model.Announcement;
import com.example.dormmanager.utils.RetrofitClient;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnnouncementRepository {
    private static final String TAG = "AnnouncementRepository";
    private final AnnouncementApiService apiService;

    public AnnouncementRepository(Context context) {
        this.apiService = RetrofitClient.getAuthenticatedClient(context)
                .create(AnnouncementApiService.class);
    }

    public void getAnnouncements(AnnouncementListCallback callback) {
        apiService.getAnnouncements().enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to fetch announcements";
                    if (response.code() == 401) {
                        errorMsg = "Authentication failed. Please log in again.";
                    } else if (response.code() == 403) {
                        errorMsg = "Access denied";
                    } else if (response.code() >= 500) {
                        errorMsg = "Server error. Please try again later.";
                    } else if (response.errorBody() != null) {
                        try {
                            errorMsg = "Error: " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Log.e(TAG, "Failed to fetch announcements. Code: " + response.code());
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + (t.getMessage() != null ? t.getMessage() : "Connection failed"));
            }
        });
    }

    public void getPinnedAnnouncements(AnnouncementListCallback callback) {
        apiService.getPinnedAnnouncements().enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch pinned announcements");
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getUrgentAnnouncements(AnnouncementListCallback callback) {
        apiService.getUrgentAnnouncements().enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch urgent announcements");
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getAnnouncementById(Long id, SingleAnnouncementCallback callback) {
        apiService.getAnnouncementById(id).enqueue(new Callback<Announcement>() {
            @Override
            public void onResponse(Call<Announcement> call, Response<Announcement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Announcement not found");
                }
            }

            @Override
            public void onFailure(Call<Announcement> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void acknowledgeAnnouncement(Long id, AcknowledgeCallback callback) {
        apiService.acknowledgeAnnouncement(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to acknowledge announcement");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void searchAnnouncements(String query, AnnouncementListCallback callback) {
        apiService.searchAnnouncements(query).enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Search failed");
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface AnnouncementListCallback {
        void onSuccess(List<Announcement> announcements);
        void onError(String error);
    }

    public interface SingleAnnouncementCallback {
        void onSuccess(Announcement announcement);
        void onError(String error);
    }

    public interface AcknowledgeCallback {
        void onSuccess();
        void onError(String error);
    }
}