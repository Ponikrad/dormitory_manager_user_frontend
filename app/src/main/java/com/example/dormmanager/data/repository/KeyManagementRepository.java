package com.example.dormmanager.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dormmanager.data.api.KeyManagementApiService;
import com.example.dormmanager.data.model.KeyAssignment;
import com.example.dormmanager.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class KeyManagementRepository {
    private static final String TAG = "KeyManagementRepo";
    private final KeyManagementApiService apiService;

    public KeyManagementRepository(Context context) {
        this.apiService = RetrofitClient.getAuthenticatedClient(context)
                .create(KeyManagementApiService.class);
    }

    public void getMyKeys(KeyListCallback callback) {
        apiService.getMyKeys().enqueue(new Callback<List<Map<String, Object>>>() {
            @Override
            public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch keys");
                }
            }

            @Override
            public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getMyAssignments(KeyAssignmentListCallback callback) {
        apiService.getMyAssignments().enqueue(new Callback<List<KeyAssignment>>() {
            @Override
            public void onResponse(Call<List<KeyAssignment>> call, Response<List<KeyAssignment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch assignments");
                }
            }

            @Override
            public void onFailure(Call<List<KeyAssignment>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void reportKeyLost(Long assignmentId, ReportLostCallback callback) {
        apiService.reportKeyLost(assignmentId).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = (String) response.body().get("message");
                    callback.onSuccess(message != null ? message : "Key reported as lost");
                } else {
                    callback.onError("Failed to report lost key");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface KeyListCallback {
        void onSuccess(List<Map<String, Object>> keys);
        void onError(String error);
    }

    public interface KeyAssignmentListCallback {
        void onSuccess(List<KeyAssignment> assignments);
        void onError(String error);
    }

    public interface ReportLostCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}