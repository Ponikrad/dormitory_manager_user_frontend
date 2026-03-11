package com.example.dormmanager.data.repository;

import android.content.Context;

import com.example.dormmanager.data.api.ChatApiService;
import com.example.dormmanager.data.model.ChatMessage;
import com.example.dormmanager.utils.RetrofitClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    private ChatApiService apiService;

    public ChatRepository(Context context) {
        this.apiService = RetrofitClient.getAuthenticatedClient(context).create(ChatApiService.class);
    }

    public interface ChatMessagesCallback {
        void onSuccess(List<ChatMessage> messages);
        void onError(String error);
    }

    public interface SendMessageCallback {
        void onSuccess(ChatMessage message);
        void onError(String error);
    }

    public void getMessages(ChatMessagesCallback callback) {
        apiService.getMessages().enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load messages");
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getMessagesAfter(String after, ChatMessagesCallback callback) {
        apiService.getMessagesAfter(after).enqueue(new Callback<List<ChatMessage>>() {
            @Override
            public void onResponse(Call<List<ChatMessage>> call, Response<List<ChatMessage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load new messages");
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessage>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void sendMessage(String content, SendMessageCallback callback) {
        Map<String, String> request = new HashMap<>();
        request.put("content", content);

        apiService.sendMessage(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> body = response.body();
                    if (Boolean.TRUE.equals(body.get("success"))) {
                        // Use the message from server response instead of creating a new one
                        Object messageObj = body.get("message");
                        if (messageObj instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> messageMap = (Map<String, Object>) messageObj;
                            ChatMessage msg = convertMapToChatMessage(messageMap);
                            callback.onSuccess(msg);
                        } else {
                            // Fallback if message format is different
                            ChatMessage msg = new ChatMessage();
                            msg.setContent(content);
                            msg.setIsFromCurrentUser(true);
                            callback.onSuccess(msg);
                        }
                    } else {
                        callback.onError("Failed to send message");
                    }
                } else {
                    callback.onError("Failed to send message");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private ChatMessage convertMapToChatMessage(Map<String, Object> messageMap) {
        ChatMessage msg = new ChatMessage();
        
        if (messageMap.get("id") != null) {
            msg.setId(((Number) messageMap.get("id")).longValue());
        }
        if (messageMap.get("senderId") != null) {
            msg.setSenderId(((Number) messageMap.get("senderId")).longValue());
        }
        if (messageMap.get("senderName") != null) {
            msg.setSenderName(messageMap.get("senderName").toString());
        }
        if (messageMap.get("senderRoomNumber") != null) {
            msg.setSenderRoomNumber(messageMap.get("senderRoomNumber").toString());
        }
        if (messageMap.get("content") != null) {
            msg.setContent(messageMap.get("content").toString());
        }
        if (messageMap.get("sentAt") != null) {
            msg.setSentAt(messageMap.get("sentAt").toString());
        }
        if (messageMap.get("isFromCurrentUser") != null) {
            msg.setIsFromCurrentUser(Boolean.TRUE.equals(messageMap.get("isFromCurrentUser")));
        }
        
        return msg;
    }
}

