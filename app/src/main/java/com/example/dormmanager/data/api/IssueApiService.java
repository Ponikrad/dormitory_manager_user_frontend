package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.CreateIssueRequest;
import com.example.dormmanager.data.model.Issue;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface IssueApiService {

    @GET("api/issues/my-issues")
    Call<Map<String, Object>> getMyIssues(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/issues/{id}")
    Call<Issue> getIssueById(@Path("id") Long id);

    @POST("api/issues/report")
    Call<Map<String, Object>> reportIssue(@Body CreateIssueRequest request);

    @POST("api/issues/{id}/reopen")
    Call<Map<String, Object>> reopenIssue(@Path("id") Long id);

    @POST("api/issues/{id}/rate")
    Call<Map<String, Object>> rateIssue(
            @Path("id") Long id,
            @Query("rating") int rating
    );

    @GET("api/issues/categories")
    Call<String[]> getCategories();

    @GET("api/issues/priorities")
    Call<String[]> getPriorities();

    @GET("api/issues/statuses")
    Call<String[]> getStatuses();
}