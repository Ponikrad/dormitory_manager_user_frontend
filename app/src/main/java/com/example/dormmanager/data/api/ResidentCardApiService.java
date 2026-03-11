package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.ResidentCard;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ResidentCardApiService {

    @GET("api/resident-cards/my-card")
    Call<ResidentCard> getMyCard();

    @POST("api/resident-cards/generate")
    Call<ResidentCard> generateCard();

    @GET("api/resident-cards/verify/{qrCode}")
    Call<Map<String, Object>> verifyQrCode(@Path("qrCode") String qrCode);

    @POST("api/resident-cards/deactivate")
    Call<Map<String, Object>> deactivateMyCard();
}