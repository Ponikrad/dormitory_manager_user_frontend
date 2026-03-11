package com.example.dormmanager.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dormmanager.data.api.ResidentCardApiService;
import com.example.dormmanager.data.model.ResidentCard;
import com.example.dormmanager.utils.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResidentCardRepository {
    private static final String TAG = "ResidentCardRepo";
    private final ResidentCardApiService apiService;

    public ResidentCardRepository(Context context) {
        this.apiService = RetrofitClient.getAuthenticatedClient(context)
                .create(ResidentCardApiService.class);
    }

    public void getMyCard(CardCallback callback) {
        apiService.getMyCard().enqueue(new Callback<ResidentCard>() {
            @Override
            public void onResponse(@NonNull Call<ResidentCard> call, @NonNull Response<ResidentCard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else if (response.code() == 404) {
                    callback.onCardNotFound();
                } else {
                    callback.onError("Failed to fetch card: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResidentCard> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void generateCard(CardCallback callback) {
        apiService.generateCard().enqueue(new Callback<ResidentCard>() {
            @Override
            public void onResponse(@NonNull Call<ResidentCard> call, @NonNull Response<ResidentCard> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to generate card: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResidentCard> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deactivateCard(SimpleCallback callback) {
        apiService.deactivateMyCard().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess("Card deactivated successfully");
                } else {
                    callback.onError("Failed to deactivate card");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface CardCallback {
        void onSuccess(ResidentCard card);
        void onCardNotFound();
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}