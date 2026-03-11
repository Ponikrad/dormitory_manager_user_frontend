package com.example.dormmanager.ui.issues;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.CreateIssueRequest;
import com.example.dormmanager.data.model.Issue;
import com.example.dormmanager.data.repository.IssueRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreateIssueActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilTitle, tilDescription, tilCategory, tilPriority;
    private AutoCompleteTextView actvCategory, actvPriority;
    private TextInputEditText etTitle, etDescription, etLocationDetails;
    private MaterialButton btnReportIssue;
    private View loadingOverlay;

    private IssueRepository issueRepository;

    private static final String[] ISSUE_CATEGORIES = {
            "PLUMBING", "ELECTRICAL", "FURNITURE", "CLEANING", "INTERNET",
            "SECURITY", "HEATING", "KITCHEN", "BATHROOM", "NOISE", "OTHER"
    };

    private static final String[] ISSUE_PRIORITIES = {
            "LOW", "MEDIUM", "HIGH", "URGENT", "CRITICAL"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_issue);

        initViews();
        setupToolbar();
        setupDropdowns();
        setupSubmitButton();

        issueRepository = new IssueRepository(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilTitle = findViewById(R.id.tilTitle);
        tilDescription = findViewById(R.id.tilDescription);
        tilCategory = findViewById(R.id.tilCategory);
        tilPriority = findViewById(R.id.tilPriority);
        actvCategory = findViewById(R.id.actvCategory);
        actvPriority = findViewById(R.id.actvPriority);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etLocationDetails = findViewById(R.id.etLocationDetails);
        btnReportIssue = findViewById(R.id.btnReportIssue);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // Category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                ISSUE_CATEGORIES
        );
        actvCategory.setAdapter(categoryAdapter);

        // Priority dropdown
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                ISSUE_PRIORITIES
        );
        actvPriority.setAdapter(priorityAdapter);
    }

    private void setupSubmitButton() {
        btnReportIssue.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        // Get values
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String priority = actvPriority.getText().toString().trim();
        String locationDetails = etLocationDetails.getText().toString().trim();

        // Create request
        CreateIssueRequest request = new CreateIssueRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setCategory(category);
        request.setPriority(priority.isEmpty() ? "MEDIUM" : priority);
        request.setLocationDetails(locationDetails.isEmpty() ? null : locationDetails);

        // Validate
        String validationError = request.getValidationError();
        if (validationError != null) {
            Toast.makeText(this, validationError, Toast.LENGTH_LONG).show();
            return;
        }

        if (category.isEmpty()) {
            tilCategory.setError("Please select a category");
            return;
        }

        // Submit
        submitIssue(request);
    }

    private void submitIssue(CreateIssueRequest request) {
        showLoading(true);

        issueRepository.reportIssue(request, new IssueRepository.CreateIssueCallback() {
            @Override
            public void onSuccess(String message, Issue issue) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showSuccessDialog();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreateIssueActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Issue Reported! 🎉")
                .setMessage("Your issue has been reported successfully. Our team will review it soon.")
                .setIcon(R.drawable.ic_issues)
                .setPositiveButton("OK", (dialog, which) -> {
                    finish(); // Return to issues list
                })
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnReportIssue.setEnabled(!show);
    }
}