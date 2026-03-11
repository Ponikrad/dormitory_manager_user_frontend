package com.example.dormmanager.data.api;

import com.example.dormmanager.data.model.Message;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface MessageApiService {

    @POST("api/messages/send")
    @FormUrlEncoded
    Call<Map<String, Object>> sendMessage(
            @Field("subject") String subject,
            @Field("content") String content,
            @Field("type") String type
    );

    @GET("api/messages/inbox")
    Call<List<Message>> getInboxMessages();

    @GET("api/messages/sent")
    Call<List<Message>> getSentMessages();

    @GET("api/messages/unread")
    Call<Map<String, Object>> getUnreadMessages();

    @GET("api/messages/{id}")
    Call<Message> getMessageById(@Path("id") Long id);

    @GET("api/messages/thread/{threadId}")
    Call<List<Message>> getMessageThread(@Path("threadId") String threadId);

    @POST("api/messages/{id}/reply")
    @FormUrlEncoded
    Call<Map<String, Object>> replyToMessage(
            @Path("id") Long messageId,
            @Field("content") String content
    );

    @PUT("api/messages/{id}/read")
    Call<Map<String, Object>> markAsRead(@Path("id") Long id);

    @DELETE("api/messages/{id}")
    Call<Map<String, Object>> deleteMessage(@Path("id") Long id);
}