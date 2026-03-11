package com.example.dormmanager.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dormmanager.data.api.IssueApiService;
import com.example.dormmanager.data.model.CreateIssueRequest;
import com.example.dormmanager.data.model.Issue;
import com.example.dormmanager.utils.RetrofitClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IssueRepository {
    private static final String TAG = "IssueRepository";
    private final IssueApiService apiService;

    public IssueRepository(Context context) {
        this.apiService = RetrofitClient.getAuthenticatedClient(context)
                .create(IssueApiService.class);
    }

    public void getMyIssues(int page, int size, IssueCallback callback) {
        apiService.getMyIssues(page, size).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> body = response.body();
                        Object issuesObj = body.get("issues");
                        List<Issue> issues = new ArrayList<>();

                        if (issuesObj instanceof List) {
                            List<?> rawList = (List<?>) issuesObj;
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            for (Object item : rawList) {
                                String json = gson.toJson(item);
                                Issue issue = gson.fromJson(json, Issue.class);
                                issues.add(issue);
                            }
                        }

                        callback.onSuccess(issues);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing issues", e);
                        callback.onError("Error parsing response: " + e.getMessage());
                    }
                } else {
                    callback.onError("Failed to fetch issues: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Log.e(TAG, "Network error", t);
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getIssueById(Long id, SingleIssueCallback callback) {
        apiService.getIssueById(id).enqueue(new Callback<Issue>() {
            @Override
            public void onResponse(Call<Issue> call, Response<Issue> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Issue not found");
                }
            }

            @Override
            public void onFailure(Call<Issue> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void reportIssue(CreateIssueRequest request, CreateIssueCallback callback) {
        apiService.reportIssue(request).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        Map<String, Object> body = response.body();
                        String message = (String) body.get("message");

                        Object issueObj = body.get("issue");
                        Issue issue = null;

                        if (issueObj != null) {
                            com.google.gson.Gson gson = new com.google.gson.Gson();
                            String json = gson.toJson(issueObj);
                            issue = gson.fromJson(json, Issue.class);
                        }

                        callback.onSuccess(message != null ? message : "Issue reported successfully", issue);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing response", e);
                        callback.onError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    callback.onError("Failed to report issue");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void reopenIssue(Long id, ReopenIssueCallback callback) {
        apiService.reopenIssue(id).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess("Issue reopened successfully");
                } else {
                    callback.onError("Failed to reopen issue");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void rateIssue(Long id, int rating, RateIssueCallback callback) {
        apiService.rateIssue(id, rating).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess("Issue rated successfully");
                } else {
                    callback.onError("Failed to rate issue");
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private List<Issue> parseIssuesList(Object issuesObj) {
        List<Issue> issues = new ArrayList<>();
        if (issuesObj instanceof List) {
            List<?> rawList = (List<?>) issuesObj;
            com.google.gson.Gson gson = new com.google.gson.Gson();
            for (Object item : rawList) {
                String json = gson.toJson(item);
                Issue issue = gson.fromJson(json, Issue.class);
                issues.add(issue);
            }
        }
        return issues;
    }

    public interface IssueCallback {
        void onSuccess(List<Issue> issues);
        void onError(String error);
    }

    public interface SingleIssueCallback {
        void onSuccess(Issue issue);
        void onError(String error);
    }

    public interface CreateIssueCallback {
        void onSuccess(String message, Issue issue);
        void onError(String error);
    }

    public interface IssueStatisticsCallback {
        void onSuccess(Map<String, Object> stats);
        void onError(String error);
    }

    public interface ReopenIssueCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface RateIssueCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}