package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.ChatMessage;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatApiService {

    @GET("api/chat/messages")
    Call<List<ChatMessage>> getMessages();

    @GET("api/chat/messages/after")
    Call<List<ChatMessage>> getMessagesAfter(@Query("after") String after);

    @POST("api/chat/send")
    Call<Map<String, Object>> sendMessage(@Body Map<String, String> request);
}

