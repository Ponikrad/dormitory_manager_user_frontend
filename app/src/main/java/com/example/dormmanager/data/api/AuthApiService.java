package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.LoginRequest;
import com.example.dormmanager.data.model.LoginResponse;
import com.example.dormmanager.data.model.RegisterRequest;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApiService {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    @GET("api/auth/profile")
    Call<Map<String, Object>> getProfile();

    @POST("api/auth/logout")
    Call<Void> logout();
}