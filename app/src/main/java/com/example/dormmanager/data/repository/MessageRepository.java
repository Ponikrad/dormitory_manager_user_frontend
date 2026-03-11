package com.example.dormmanager.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dormmanager.data.api.MessageApiService;
import com.example.dormmanager.data.model.Message;
import com.example.dormmanager.utils.RetrofitClient;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageRepository {
    private static final String TAG = "MessageRepository";
    private final MessageApiService apiService;

    public MessageRepository(Context context) {
        this.apiService = RetrofitClient.getAuthenticatedClient(context)
                .create(MessageApiService.class);
    }

    public void sendMessage(String subject, String content, String type, SendMessageCallback callback) {
        apiService.sendMessage(subject, content, type).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = (String) response.body().get("message");
                    callback.onSuccess(message != null ? message : "Message sent successfully");
                } else {
                    callback.onError("Failed to send message");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getInboxMessages(MessageListCallback callback) {
        apiService.getInboxMessages().enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch messages");
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getSentMessages(MessageListCallback callback) {
        apiService.getSentMessages().enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to fetch sent messages");
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getUnreadMessages(UnreadMessagesCallback callback) {
        apiService.getUnreadMessages().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> data = response.body();
                    Number count = (Number) data.get("count");
                    callback.onSuccess(count != null ? count.intValue() : 0);
                } else {
                    callback.onError("Failed to fetch unread count");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getMessageById(Long id, SingleMessageCallback callback) {
        apiService.getMessageById(id).enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Message not found");
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void replyToMessage(Long messageId, String content, ReplyCallback callback) {
        apiService.replyToMessage(messageId, content).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = (String) response.body().get("message");
                    callback.onSuccess(message != null ? message : "Reply sent");
                } else {
                    callback.onError("Failed to send reply");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void markAsRead(Long id, MarkReadCallback callback) {
        apiService.markAsRead(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Failed to mark as read");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public interface SendMessageCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface MessageListCallback {
        void onSuccess(List<Message> messages);
        void onError(String error);
    }

    public interface SingleMessageCallback {
        void onSuccess(Message message);
        void onError(String error);
    }

    public interface UnreadMessagesCallback {
        void onSuccess(int count);
        void onError(String error);
    }

    public interface ReplyCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface MarkReadCallback {
        void onSuccess();
        void onError(String error);
    }
}