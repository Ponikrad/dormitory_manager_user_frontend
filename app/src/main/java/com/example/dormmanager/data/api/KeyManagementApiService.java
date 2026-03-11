package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.KeyAssignment;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface KeyManagementApiService {

    @GET("api/keys/my")
    Call<List<Map<String, Object>>> getMyKeys();

    @GET("api/keys/assignments/my")
    Call<List<KeyAssignment>> getMyAssignments();

    @POST("api/keys/assignments/{assignmentId}/report-lost")
    Call<Map<String, Object>> reportKeyLost(@Path("assignmentId") Long assignmentId);
}