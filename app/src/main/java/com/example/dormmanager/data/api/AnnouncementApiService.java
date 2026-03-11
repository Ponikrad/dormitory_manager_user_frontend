package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.Announcement;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface AnnouncementApiService {

    @GET("api/announcements")
    Call<List<Announcement>> getAnnouncements();

    @GET("api/announcements/pinned")
    Call<List<Announcement>> getPinnedAnnouncements();

    @GET("api/announcements/urgent")
    Call<List<Announcement>> getUrgentAnnouncements();

    @GET("api/announcements/{id}")
    Call<Announcement> getAnnouncementById(@Path("id") Long id);

    @POST("api/announcements/{id}/acknowledge")
    Call<Map<String, Object>> acknowledgeAnnouncement(@Path("id") Long id);

    @GET("api/announcements/search")
    Call<List<Announcement>> searchAnnouncements(@Query("query") String query);

    @GET("api/announcements/types")
    Call<List<Map<String, Object>>> getAnnouncementTypes();

    @GET("api/announcements/priorities")
    Call<List<Map<String, Object>>> getAnnouncementPriorities();
}