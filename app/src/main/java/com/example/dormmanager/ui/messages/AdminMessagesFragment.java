package com.example.dormmanager.ui.messages;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Message;
import com.example.dormmanager.data.repository.MessageRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class AdminMessagesFragment extends Fragment implements MessageAdapter.OnMessageClickListener {

    private static final int ITEMS_PER_PAGE = 6;

    private RecyclerView recyclerViewMessages;
    private MessageAdapter adapter;
    private LinearLayout layoutEmpty;
    private LinearLayout paginationContainer;
    private TextView tvUnreadCount, tvTotalCount, tvPageInfo;
    private MaterialButton btnNewMessage, btnPrevPage, btnNextPage;
    private CircularProgressIndicator progressBar;
    private Chip chipInbox, chipSent, chipUnread;

    private MessageRepository messageRepository;
    private String currentFilter = "INBOX";
    private int currentPage = 0;
    private int totalPages = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_messages, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        setupFilters();
        setupButtons();
        
        messageRepository = new MessageRepository(requireContext());
        loadMessages();
    }

    private void initViews(View view) {
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        paginationContainer = view.findViewById(R.id.paginationContainer);
        tvUnreadCount = view.findViewById(R.id.tvUnreadCount);
        tvTotalCount = view.findViewById(R.id.tvTotalCount);
        tvPageInfo = view.findViewById(R.id.tvPageInfo);
        btnNewMessage = view.findViewById(R.id.btnNewMessage);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        chipInbox = view.findViewById(R.id.chipInbox);
        chipSent = view.findViewById(R.id.chipSent);
        chipUnread = view.findViewById(R.id.chipUnread);
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(requireContext(), this);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewMessages.setAdapter(adapter);
    }

    private void setupFilters() {
        chipInbox.setOnClickListener(v -> filterMessages("INBOX"));
        chipSent.setOnClickListener(v -> filterMessages("SENT"));
        chipUnread.setOnClickListener(v -> filterMessages("UNREAD"));
    }

    private void setupButtons() {
        btnNewMessage.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SendMessageActivity.class);
            startActivity(intent);
        });

        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                displayCurrentPage();
            }
        });

        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                displayCurrentPage();
            }
        });
    }

    private void filterMessages(String filter) {
        currentFilter = filter;
        currentPage = 0;
        loadMessages();
    }

    private void loadMessages() {
        if ("INBOX".equals(currentFilter)) {
            loadInboxMessages();
        } else if ("SENT".equals(currentFilter)) {
            loadSentMessages();
        } else if ("UNREAD".equals(currentFilter)) {
            loadUnreadMessages();
        }
    }

    private void loadInboxMessages() {
        messageRepository.getInboxMessages(new MessageRepository.MessageListCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.setMessages(messages);
                        updateSummary(messages);
                        displayCurrentPage();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        layoutEmpty.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    private void loadSentMessages() {
        messageRepository.getSentMessages(new MessageRepository.MessageListCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        adapter.setMessages(messages);
                        updateSummary(messages);
                        displayCurrentPage();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void loadUnreadMessages() {
        messageRepository.getInboxMessages(new MessageRepository.MessageListCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Filter only unread
                        java.util.List<Message> unreadMessages = new java.util.ArrayList<>();
                        for (Message message : messages) {
                            if (message.isUnread()) {
                                unreadMessages.add(message);
                            }
                        }
                        adapter.setMessages(unreadMessages);
                        updateSummary(unreadMessages);
                        displayCurrentPage();
                    });
                }
            }

            @Override
            public void onError(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void displayCurrentPage() {
        int totalItems = adapter.getTotalItemCount();
        totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0) currentPage = 0;
        
        adapter.setPage(currentPage, ITEMS_PER_PAGE);
        
        if (totalItems > ITEMS_PER_PAGE) {
            paginationContainer.setVisibility(View.VISIBLE);
            tvPageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
            btnPrevPage.setEnabled(currentPage > 0);
            btnNextPage.setEnabled(currentPage < totalPages - 1);
        } else {
            paginationContainer.setVisibility(View.GONE);
        }
        
        updateEmptyState();
    }

    private void updateSummary(List<Message> messages) {
        int unreadCount = 0;
        for (Message message : messages) {
            if (message.isUnread()) {
                unreadCount++;
            }
        }
        tvUnreadCount.setText(String.valueOf(unreadCount));
        tvTotalCount.setText(String.valueOf(messages.size()));
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerViewMessages.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewMessages.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMessageClick(Message message) {
        Intent intent = new Intent(requireContext(), MessageDetailsActivity.class);
        intent.putExtra("message_id", message.getId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMessages();
    }
}

