package com.example.dormmanager.ui.messages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.ChatMessage;
import com.example.dormmanager.data.repository.ChatRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ResidentChatFragment extends Fragment {

    private static final long POLL_INTERVAL = 5000; // 5 seconds

    private RecyclerView recyclerViewChat;
    private ChatMessageAdapter adapter;
    private LinearLayout layoutEmpty;
    private CircularProgressIndicator progressBar;
    private TextInputEditText etMessage;
    private FloatingActionButton fabSend;

    private ChatRepository chatRepository;
    private Handler pollHandler;
    private Runnable pollRunnable;
    private String lastMessageTime = null;
    private boolean isPolling = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resident_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupSendButton();

        chatRepository = new ChatRepository(requireContext());
        pollHandler = new Handler(Looper.getMainLooper());

        loadMessages();
    }

    private void initViews(View view) {
        recyclerViewChat = view.findViewById(R.id.recyclerViewChat);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        etMessage = view.findViewById(R.id.etMessage);
        fabSend = view.findViewById(R.id.fabSend);
    }

    private void setupRecyclerView() {
        adapter = new ChatMessageAdapter(requireContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(adapter);
    }

    private void setupSendButton() {
        fabSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        showLoading(true);

        chatRepository.getMessages(new ChatRepository.ChatMessagesCallback() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        adapter.setMessages(messages);
                        updateEmptyState();
                        scrollToBottom();

                        // Set last message time for polling
                        if (!messages.isEmpty()) {
                            lastMessageTime = messages.get(messages.size() - 1).getSentAt();
                        }

                        // Start polling for new messages
                        startPolling();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        updateEmptyState();
                    });
                }
            }
        });
    }

    private void sendMessage() {
        String content = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";

        if (content.isEmpty()) {
            return;
        }

        // Disable send button temporarily
        fabSend.setEnabled(false);

        chatRepository.sendMessage(content, new ChatRepository.SendMessageCallback() {
            @Override
            public void onSuccess(ChatMessage message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        fabSend.setEnabled(true);
                        etMessage.setText("");
                        
                        // Add message to list optimistically
                        adapter.addMessage(message);
                        updateEmptyState();
                        scrollToBottom();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        fabSend.setEnabled(true);
                        Toast.makeText(requireContext(), "Failed to send: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void startPolling() {
        if (isPolling) return;
        isPolling = true;

        pollRunnable = new Runnable() {
            @Override
            public void run() {
                pollNewMessages();
                pollHandler.postDelayed(this, POLL_INTERVAL);
            }
        };

        pollHandler.postDelayed(pollRunnable, POLL_INTERVAL);
    }

    private void stopPolling() {
        isPolling = false;
        if (pollHandler != null && pollRunnable != null) {
            pollHandler.removeCallbacks(pollRunnable);
        }
    }

    private void pollNewMessages() {
        if (lastMessageTime == null) {
            return;
        }

        chatRepository.getMessagesAfter(lastMessageTime, new ChatRepository.ChatMessagesCallback() {
            @Override
            public void onSuccess(List<ChatMessage> messages) {
                if (getActivity() != null && !messages.isEmpty()) {
                    getActivity().runOnUiThread(() -> {
                        for (ChatMessage message : messages) {
                            adapter.addMessage(message);
                        }
                        updateEmptyState();
                        scrollToBottom();

                        // Update last message time
                        lastMessageTime = messages.get(messages.size() - 1).getSentAt();
                    });
                }
            }

            @Override
            public void onError(String error) {
                // Silently fail - will retry on next poll
            }
        });
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            recyclerViewChat.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerViewChat.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewChat.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload messages when returning to fragment to avoid duplicates
        loadMessages();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPolling();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPolling();
    }
}

