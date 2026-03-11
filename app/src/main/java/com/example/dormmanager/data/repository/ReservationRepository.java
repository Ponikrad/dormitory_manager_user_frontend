package com.example.dormmanager.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dormmanager.data.api.ReservationApiService;
import com.example.dormmanager.data.model.CreateReservationRequest;
import com.example.dormmanager.data.model.Reservation;
import com.example.dormmanager.data.model.ReservableResource;
import com.example.dormmanager.utils.RetrofitClient;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationRepository {
    private static final String TAG = "ReservationRepository";
    private final ReservationApiService apiService;

    public ReservationRepository(Context context) {
        this.apiService = RetrofitClient.getAuthenticatedClient(context)
                .create(ReservationApiService.class);
    }

    public void createReservation(CreateReservationRequest request, CreateReservationCallback callback) {
        apiService.createReservation(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> body = response.body();
                        String message = (String) body.get("message");

                        Object reservationObj = body.get("reservation");
                        Reservation reservation = null;

                        if (reservationObj != null) {
                            Gson gson = new Gson();
                            String json = gson.toJson(reservationObj);
                            reservation = gson.fromJson(json, Reservation.class);
                        }

                        callback.onSuccess(message != null ? message : "Reservation created", reservation);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    String errorMessage = parseErrorResponse(response);
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private String parseErrorResponse(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();

                try {
                    Gson gson = new Gson();
                    Map<String, Object> errorMap = gson.fromJson(errorBody, Map.class);

                    if (errorMap.containsKey("message")) {
                        return (String) errorMap.get("message");
                    } else if (errorMap.containsKey("error")) {
                        return (String) errorMap.get("error");
                    }
                } catch (Exception e) {
                    return errorBody;
                }
            }

            switch (response.code()) {
                case 400:
                    return "Invalid reservation data. Please check your input.";
                case 401:
                    return "Authentication required. Please log in again.";
                case 403:
                    return "You don't have permission to make this reservation.";
                case 404:
                    return "Resource not found.";
                case 409:
                    return "This resource is already reserved for the selected time.";
                case 500:
                    return "Server error. Please try again later.";
                default:
                    return "Failed to create reservation (Error " + response.code() + ")";
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing error response", e);
            return "An error occurred (HTTP " + response.code() + ")";
        }
    }

    public void getMyReservations(ReservationListCallback callback) {
        apiService.getMyReservations().enqueue(new Callback<List<Reservation>>() {
            @Override
            public void onResponse(Call<List<Reservation>> call, Response<List<Reservation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch reservations");
                }
            }

            @Override
            public void onFailure(Call<List<Reservation>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getReservationById(Long id, SingleReservationCallback callback) {
        apiService.getReservationById(id).enqueue(new Callback<Reservation>() {
            @Override
            public void onResponse(Call<Reservation> call, Response<Reservation> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Reservation not found");
                }
            }

            @Override
            public void onFailure(Call<Reservation> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void cancelReservation(Long id, CancelReservationCallback callback) {
        apiService.cancelReservation(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = (String) response.body().get("message");
                    callback.onSuccess(message != null ? message : "Reservation cancelled");
                } else {
                    callback.onError("Failed to cancel reservation");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void checkInReservation(Long id, CheckInCallback callback) {
        apiService.checkInReservation(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = (String) response.body().get("message");
                    callback.onSuccess(message != null ? message : "Checked in successfully");
                } else {
                    callback.onError("Failed to check-in");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getAllResources(ResourceListCallback callback) {
        apiService.getAllResources().enqueue(new Callback<List<ReservableResource>>() {
            @Override
            public void onResponse(Call<List<ReservableResource>> call, Response<List<ReservableResource>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch resources");
                }
            }

            @Override
            public void onFailure(Call<List<ReservableResource>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getAvailableResources(String startTime, String endTime, ResourceListCallback callback) {
        apiService.getAvailableResources(startTime, endTime).enqueue(new Callback<List<ReservableResource>>() {
            @Override
            public void onResponse(Call<List<ReservableResource>> call, Response<List<ReservableResource>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch available resources");
                }
            }

            @Override
            public void onFailure(Call<List<ReservableResource>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface CreateReservationCallback {
        void onSuccess(String message, Reservation reservation);
        void onError(String error);
    }

    public interface ReservationListCallback {
        void onSuccess(List<Reservation> reservations);
        void onError(String error);
    }

    public interface SingleReservationCallback {
        void onSuccess(Reservation reservation);
        void onError(String error);
    }

    public interface CancelReservationCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface CheckInCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ResourceListCallback {
        void onSuccess(List<ReservableResource> resources);
        void onError(String error);
    }
}