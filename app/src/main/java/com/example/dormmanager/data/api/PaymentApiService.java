package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.CreatePaymentRequest;
import com.example.dormmanager.data.model.Payment;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface PaymentApiService {

    @GET("api/payments/my-payments")
    Call<Map<String, Object>> getMyPayments(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/payments/{id}")
    Call<Payment> getPaymentById(@Path("id") Long id);

    @POST("api/payments/create")
    Call<Map<String, Object>> createPayment(@Body CreatePaymentRequest request);

    @POST("api/payments/{id}/process")
    Call<Map<String, Object>> processPayment(@Path("id") Long id);

    @Streaming
    @GET("api/payments/{id}/receipt")
    Call<ResponseBody> getPaymentReceipt(@Path("id") Long id);

    @GET("api/payments/pending")
    Call<List<Payment>> getPendingPayments();

    @GET("api/payments/stats")
    Call<Map<String, Object>> getPaymentStatistics();
}