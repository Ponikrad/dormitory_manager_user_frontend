package com.example.dormmanager.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.dormmanager.data.api.AuthApiService;
import com.example.dormmanager.data.local.TokenManager;
import com.example.dormmanager.data.model.LoginRequest;
import com.example.dormmanager.data.model.LoginResponse;
import com.example.dormmanager.data.model.RegisterRequest;
import com.example.dormmanager.data.model.User;
import com.example.dormmanager.utils.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private AuthApiService apiService;
    private TokenManager tokenManager;
    private Context context;

    public AuthRepository(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getClient().create(AuthApiService.class);
        this.tokenManager = new TokenManager(context);
    }

    public void login(String username, String password, AuthCallback<LoginResponse> callback) {
        LoginRequest request = new LoginRequest(username, password);

        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    User user = loginResponse.getUser();

                    if (user != null && user.getRole() != null) {
                        String role = user.getRole().toUpperCase();
                        if (role.equals("ADMIN") || role.equals("RECEPTIONIST")) {
                            callback.onError("This app is only for students. Please use the web portal for staff access.");
                            return;
                        }
                        if (!role.equals("STUDENT")) {
                            callback.onError("Invalid account type. Only students can use this app.");
                            return;
                        }
                    }

                    tokenManager.saveToken(loginResponse.getToken());
                    tokenManager.saveUser(loginResponse.getUser());
                    callback.onSuccess(loginResponse);
                } else {
                    callback.onError("Login failed. Please check your credentials.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void register(RegisterRequest request, AuthCallback<LoginResponse> callback) {
        apiService.register(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse registerResponse = response.body();
                    User user = registerResponse.getUser();

                    if (user != null && user.getRole() != null) {
                        if (!user.getRole().toUpperCase().equals("STUDENT")) {
                            callback.onError("Registration failed. Only students can register through the app.");
                            return;
                        }
                    }

                    tokenManager.saveToken(registerResponse.getToken());
                    tokenManager.saveUser(registerResponse.getUser());
                    callback.onSuccess(registerResponse);
                } else {
                    callback.onError("Registration failed. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getProfile(AuthCallback<User> callback) {
        String token = tokenManager.getToken();
        if (token == null) {
            callback.onError("Not logged in (No token)");
            return;
        }

        AuthApiService authenticatedApi = RetrofitClient.getAuthenticatedClient(context)
                .create(AuthApiService.class);

        authenticatedApi.getProfile().enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<Map<String, Object>> call, @NonNull Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> userMap = response.body();
                        User user = parseUserFromMap(userMap);
                        tokenManager.saveUser(user);
                        callback.onSuccess(user);
                    } catch (Exception e) {
                        callback.onError("Failed to parse profile: " + e.getMessage());
                    }
                } else if (response.code() == 401) {
                    tokenManager.clearAll();
                    callback.onError("401 Unauthorized - Session expired");
                } else {
                    callback.onError("Failed to load profile (Code: " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Map<String, Object>> call, @NonNull Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private User parseUserFromMap(Map<String, Object> userMap) {
        User user = new User();
        user.setId(getLong(userMap.get("id")));
        user.setUsername(getString(userMap.get("username")));
        user.setEmail(getString(userMap.get("email")));
        user.setFirstName(getString(userMap.get("firstName")));
        user.setLastName(getString(userMap.get("lastName")));
        user.setPhoneNumber(getString(userMap.get("phoneNumber")));
        user.setRoomNumber(getString(userMap.get("roomNumber")));
        user.setRole(getString(userMap.get("role")));
        user.setActive(getBoolean(userMap.get("active")));
        user.setCreatedAt(getString(userMap.get("createdAt")));
        return user;
    }

    private String getString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private Long getLong(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) return ((Number) obj).longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean getBoolean(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean) return (Boolean) obj;
        return Boolean.parseBoolean(obj.toString());
    }

    public void logout() {
        tokenManager.clearAll();
        RetrofitClient.clearInstances();
    }

    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    public interface AuthCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}