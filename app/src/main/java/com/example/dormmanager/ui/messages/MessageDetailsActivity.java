package com.example.dormmanager.ui.messages;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Message;
import com.example.dormmanager.data.repository.MessageRepository;
import com.example.dormmanager.utils.DateTimeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

public class MessageDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvIcon, tvSubject, tvSender, tvRecipient, tvContent;
    private TextView tvSentDate, tvMessageType;
    private Chip chipStatus, chipPriority;
    private MaterialButton btnReply;
    private CircularProgressIndicator progressBar;
    private View cardReply;
    private TextInputEditText etReplyContent;
    private MaterialButton btnSendReply, btnCancelReply;

    private MessageRepository messageRepository;
    private Message currentMessage;
    private Long messageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_details);

        initViews();
        setupToolbar();

        messageRepository = new MessageRepository(this);

        // Get message ID from intent
        messageId = getIntent().getLongExtra("message_id", -1);
        if (messageId == -1) {
            Toast.makeText(this, "Invalid message", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadMessageDetails();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvIcon = findViewById(R.id.tvIcon);
        tvSubject = findViewById(R.id.tvSubject);
        tvSender = findViewById(R.id.tvSender);
        tvRecipient = findViewById(R.id.tvRecipient);
        tvContent = findViewById(R.id.tvContent);
        tvSentDate = findViewById(R.id.tvSentDate);
        tvMessageType = findViewById(R.id.tvMessageType);
        chipStatus = findViewById(R.id.chipStatus);
        chipPriority = findViewById(R.id.chipPriority);
        btnReply = findViewById(R.id.btnReply);
        progressBar = findViewById(R.id.progressBar);
        cardReply = findViewById(R.id.cardReply);
        etReplyContent = findViewById(R.id.etReplyContent);
        btnSendReply = findViewById(R.id.btnSendReply);
        btnCancelReply = findViewById(R.id.btnCancelReply);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupButtons() {
        btnReply.setOnClickListener(v -> showReplyCard());
        btnSendReply.setOnClickListener(v -> sendReply());
        btnCancelReply.setOnClickListener(v -> hideReplyCard());
    }

    private void loadMessageDetails() {
        showLoading(true);

        messageRepository.getMessageById(messageId, new MessageRepository.SingleMessageCallback() {
            @Override
            public void onSuccess(Message message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    currentMessage = message;
                    displayMessageDetails(message);

                    // Mark as read
                    if (message.isUnread()) {
                        markAsRead();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(MessageDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayMessageDetails(Message message) {
        // Icon
        tvIcon.setText(message.getMessageIcon());

        // Subject
        tvSubject.setText(message.getSubject() != null ? message.getSubject() : "No Subject");

        // Sender and recipient
        tvSender.setText("From: " + (message.getSenderName() != null ?
                message.getSenderName() : "Unknown"));

        String recipientText = message.getRecipientName() != null ?
                message.getRecipientName() : "Administration";
        tvRecipient.setText("To: " + recipientText);

        // Content
        tvContent.setText(message.getContent() != null ? message.getContent() : "");

        // Date
        tvSentDate.setText("Sent: " + DateTimeUtils.formatDateTime(message.getSentAt()));

        // Message type
        if (message.getMessageType() != null) {
            tvMessageType.setText("Type: " + message.getMessageType().replace("_", " "));
            tvMessageType.setVisibility(View.VISIBLE);
        } else {
            tvMessageType.setVisibility(View.GONE);
        }

        // Status
        chipStatus.setText(message.getStatusDisplay());

        // Priority
        if (message.isUrgent() || (message.getPriority() != null && message.getPriority() > 3)) {
            chipPriority.setVisibility(View.VISIBLE);
            chipPriority.setText(message.isUrgent() ? "Urgent" : "High Priority");
            chipPriority.setChipBackgroundColorResource(android.R.color.holo_red_light);
        } else {
            chipPriority.setVisibility(View.GONE);
        }

        // Reply button - show only if from admin or needs response
        btnReply.setVisibility(message.isFromAdmin() || message.needsResponse() ?
                View.VISIBLE : View.GONE);
    }

    private void markAsRead() {
        messageRepository.markAsRead(messageId, new MessageRepository.MarkReadCallback() {
            @Override
            public void onSuccess() {
                // Message marked as read
            }

            @Override
            public void onError(String error) {
                // Ignore error
            }
        });
    }

    private void showReplyCard() {
        cardReply.setVisibility(View.VISIBLE);
        btnReply.setVisibility(View.GONE);
        etReplyContent.requestFocus();
    }

    private void hideReplyCard() {
        cardReply.setVisibility(View.GONE);
        btnReply.setVisibility(View.VISIBLE);
        etReplyContent.setText("");
    }

    private void sendReply() {
        String replyContent = etReplyContent.getText().toString().trim();

        if (replyContent.isEmpty()) {
            Toast.makeText(this, "Please enter your reply", Toast.LENGTH_SHORT).show();
            return;
        }

        if (replyContent.length() < 10) {
            Toast.makeText(this, "Reply must be at least 10 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        messageRepository.replyToMessage(messageId, replyContent,
                new MessageRepository.ReplyCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            showLoading(false);

                            new MaterialAlertDialogBuilder(MessageDetailsActivity.this)
                                    .setTitle("Reply Sent! ✅")
                                    .setMessage("Your reply has been sent successfully.")
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        hideReplyCard();
                                        loadMessageDetails(); // Refresh
                                    })
                                    .show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(MessageDetailsActivity.this,
                                    "Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}