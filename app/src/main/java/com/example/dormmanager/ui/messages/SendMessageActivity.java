package com.example.dormmanager.ui.messages;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.SendMessageRequest;
import com.example.dormmanager.data.repository.MessageRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SendMessageActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilSubject, tilMessageType;
    private TextInputEditText etSubject, etContent;
    private AutoCompleteTextView actvMessageType;
    private MaterialButton btnSend;
    private View loadingOverlay;

    private MessageRepository messageRepository;

    private final String[] MESSAGE_TYPES = {
            "INQUIRY - General Question",
            "COMPLAINT - Issue or Problem",
            "REQUEST - Service Request",
            "MAINTENANCE - Maintenance Related",
            "PAYMENT - Payment Inquiry",
            "RESERVATION - Reservation Related",
            "OTHER - Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        initViews();
        setupToolbar();
        setupMessageTypeDropdown();
        setupButtons();

        messageRepository = new MessageRepository(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilSubject = findViewById(R.id.tilSubject);
        tilMessageType = findViewById(R.id.tilMessageType);
        etSubject = findViewById(R.id.etSubject);
        etContent = findViewById(R.id.etContent);
        actvMessageType = findViewById(R.id.actvMessageType);
        btnSend = findViewById(R.id.btnSend);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupMessageTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                MESSAGE_TYPES
        );
        actvMessageType.setAdapter(adapter);
        actvMessageType.setText(MESSAGE_TYPES[0], false); // Default to INQUIRY
    }

    private void setupButtons() {
        btnSend.setOnClickListener(v -> validateAndSend());
    }

    private void validateAndSend() {
        String subject = etSubject.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String selectedType = actvMessageType.getText().toString();

        // Extract type from selection
        String type = "INQUIRY"; // Default
        if (selectedType.startsWith("COMPLAINT")) {
            type = "COMPLAINT";
        } else if (selectedType.startsWith("REQUEST")) {
            type = "REQUEST";
        } else if (selectedType.startsWith("MAINTENANCE")) {
            type = "MAINTENANCE";
        } else if (selectedType.startsWith("PAYMENT")) {
            type = "PAYMENT";
        } else if (selectedType.startsWith("RESERVATION")) {
            type = "RESERVATION";
        } else if (selectedType.startsWith("OTHER")) {
            type = "OTHER";
        }

        SendMessageRequest request = new SendMessageRequest(subject, content, type);

        String validationError = request.getValidationError();
        if (validationError != null) {
            Toast.makeText(this, validationError, Toast.LENGTH_LONG).show();
            return;
        }

        sendMessage(subject, content, type);
    }

    private void sendMessage(String subject, String content, String type) {
        showLoading(true);

        messageRepository.sendMessage(subject, content, type, new MessageRepository.SendMessageCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showSuccessDialog();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(SendMessageActivity.this,
                            "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Message Sent! ✅")
                .setMessage("Your message has been sent to administration. " +
                        "You will receive a response soon.")
                .setIcon(R.drawable.ic_issues)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!show);
    }
}