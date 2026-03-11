package com.example.dormmanager.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dormmanager.data.api.PaymentApiService;
import com.example.dormmanager.data.model.CreatePaymentRequest;
import com.example.dormmanager.data.model.Payment;
import com.example.dormmanager.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentRepository {
    private static final String TAG = "PaymentRepository";
    private final PaymentApiService apiService;

    public PaymentRepository(Context context) {
        this.apiService = RetrofitClient.getAuthenticatedClient(context)
                .create(PaymentApiService.class);
    }

    public void getMyPayments(int page, int size, PaymentCallback callback) {
        apiService.getMyPayments(page, size).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> body = response.body();

                        Object paymentsObj = body.get("payments");
                        List<Payment> payments = new ArrayList<>();

                        if (paymentsObj instanceof List) {
                            List<?> rawList = (List<?>) paymentsObj;

                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            for (Object item : rawList) {
                                String json = gson.toJson(item);
                                Payment payment = gson.fromJson(json, Payment.class);
                                payments.add(payment);
                            }
                        }

                        callback.onSuccess(payments);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing payments", e);
                        callback.onError("Error parsing response: " + e.getMessage());
                    }
                } else {
                    callback.onError("Failed to fetch payments: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getPaymentById(Long id, SinglePaymentCallback callback) {
        apiService.getPaymentById(id).enqueue(new Callback<Payment>() {
            @Override
            public void onResponse(Call<Payment> call, Response<Payment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Payment not found");
                }
            }

            @Override
            public void onFailure(Call<Payment> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createPayment(CreatePaymentRequest request, CreatePaymentCallback callback) {
        apiService.createPayment(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    String message = (String) body.get("message");
                    callback.onSuccess(message != null ? message : "Payment created successfully");
                } else {
                    callback.onError("Failed to create payment");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void processPayment(Long id, ProcessPaymentCallback callback) {
        apiService.processPayment(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess("Payment processed successfully");
                } else {
                    callback.onError("Failed to process payment");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void downloadReceipt(Long id, ReceiptCallback callback) {
        apiService.getPaymentReceipt(id).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Check content type
                    String contentType = response.headers().get("Content-Type");
                    if (contentType != null && contentType.contains("application/pdf")) {
                        callback.onSuccess(response.body());
                    } else {
                        Log.w(TAG, "Unexpected content type: " + contentType);
                        callback.onSuccess(response.body()); // Try anyway
                    }
                } else {
                    String errorMsg = "Failed to download receipt";
                    if (response.code() == 404) {
                        errorMsg = "Receipt not found";
                    } else if (response.code() == 403) {
                        errorMsg = "Access denied";
                    } else if (response.code() >= 500) {
                        errorMsg = "Server error";
                    }
                    Log.e(TAG, "Error downloading receipt: " + response.code() + " - " + response.message());
                    callback.onError(errorMsg + " (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Network error downloading receipt", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface PaymentCallback {
        void onSuccess(List<Payment> payments);
        void onError(String error);
    }

    public interface SinglePaymentCallback {
        void onSuccess(Payment payment);
        void onError(String error);
    }

    public interface CreatePaymentCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ProcessPaymentCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ReceiptCallback {
        void onSuccess(ResponseBody body);
        void onError(String error);
    }
}