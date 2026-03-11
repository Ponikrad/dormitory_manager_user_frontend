package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.User;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ProfileApiService {

    @Multipart
    @POST("api/profile/image")
    Call<Map<String, Object>> uploadProfileImage(@Part MultipartBody.Part image);

    @GET("api/profile/image")
    Call<ResponseBody> getProfileImage();

    @GET("api/profile/image/{userId}")
    Call<ResponseBody> getProfileImageByUserId(@Path("userId") Long userId);

    @DELETE("api/profile/image")
    Call<Map<String, Object>> deleteProfileImage();

    @PUT("api/profile/update")
    Call<Map<String, Object>> updateProfile(@Body Map<String, String> updateData);

    @PUT("api/profile/change-password")
    Call<Map<String, Object>> changePassword(@Body Map<String, String> passwords);
}