package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.CreateReservationRequest;
import com.example.dormmanager.data.model.Reservation;
import com.example.dormmanager.data.model.ReservableResource;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ReservationApiService {

    @POST("api/reservations")
    Call<Map<String, Object>> createReservation(@Body CreateReservationRequest request);

    @GET("api/reservations/my")
    Call<List<Reservation>> getMyReservations();

    @GET("api/reservations/{id}")
    Call<Reservation> getReservationById(@Path("id") Long id);

    @POST("api/reservations/{id}/cancel")
    Call<Map<String, Object>> cancelReservation(@Path("id") Long id);

    @POST("api/reservations/{id}/checkin")
    Call<Map<String, Object>> checkInReservation(@Path("id") Long id);

    @GET("api/resources")
    Call<List<ReservableResource>> getAllResources();

    @GET("api/resources/available")
    Call<List<ReservableResource>> getAvailableResources(
            @Query("startTime") String startTime,
            @Query("endTime") String endTime
    );
}