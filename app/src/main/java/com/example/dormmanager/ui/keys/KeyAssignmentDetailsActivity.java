package com.example.dormmanager.ui.keys;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.KeyAssignment;
import com.example.dormmanager.data.repository.KeyManagementRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class KeyAssignmentDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvIcon, tvKeyCode, tvKeyDescription;
    private TextView tvAssignmentType, tvIssuedDate, tvExpectedReturn;
    private TextView tvIssuedBy, tvCondition, tvNotes, tvDepositInfo;
    private Chip chipStatus;
    private MaterialButton btnReportLost;
    private CircularProgressIndicator progressBar;
    private View cardDeposit, cardWarning;
    private TextView tvWarningText;

    private KeyManagementRepository keyRepository;
    private KeyAssignment currentAssignment;
    private Long assignmentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_assignment_details);

        initViews();
        setupToolbar();

        keyRepository = new KeyManagementRepository(this);

        // Get assignment ID from intent
        assignmentId = getIntent().getLongExtra("assignment_id", -1);
        if (assignmentId == -1) {
            Toast.makeText(this, "Invalid key assignment", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAssignmentDetails();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvIcon = findViewById(R.id.tvIcon);
        tvKeyCode = findViewById(R.id.tvKeyCode);
        tvKeyDescription = findViewById(R.id.tvKeyDescription);
        tvAssignmentType = findViewById(R.id.tvAssignmentType);
        tvIssuedDate = findViewById(R.id.tvIssuedDate);
        tvExpectedReturn = findViewById(R.id.tvExpectedReturn);
        tvIssuedBy = findViewById(R.id.tvIssuedBy);
        tvCondition = findViewById(R.id.tvCondition);
        tvNotes = findViewById(R.id.tvNotes);
        tvDepositInfo = findViewById(R.id.tvDepositInfo);
        chipStatus = findViewById(R.id.chipStatus);
        btnReportLost = findViewById(R.id.btnReportLost);
        progressBar = findViewById(R.id.progressBar);
        cardDeposit = findViewById(R.id.cardDeposit);
        cardWarning = findViewById(R.id.cardWarning);
        tvWarningText = findViewById(R.id.tvWarningText);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupButtons() {
        btnReportLost.setOnClickListener(v -> reportKeyLost());
    }

    private void loadAssignmentDetails() {
        showLoading(true);

        // Since we don't have a direct endpoint for single assignment,
        // we need to get all assignments and find the one we need
        keyRepository.getMyAssignments(new KeyManagementRepository.KeyAssignmentListCallback() {
            @Override
            public void onSuccess(List<KeyAssignment> assignments) {
                runOnUiThread(() -> {
                    showLoading(false);

                    // Find the assignment
                    for (KeyAssignment assignment : assignments) {
                        if (assignment.getId().equals(assignmentId)) {
                            currentAssignment = assignment;
                            displayAssignmentDetails(assignment);
                            return;
                        }
                    }

                    // Assignment not found
                    Toast.makeText(KeyAssignmentDetailsActivity.this,
                            "Assignment not found", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(KeyAssignmentDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayAssignmentDetails(KeyAssignment assignment) {
        // Icon
        tvIcon.setText(assignment.getKeyIcon());

        // Key code and description
        tvKeyCode.setText(assignment.getKeyCode() != null ? assignment.getKeyCode() : "Key");

        if (assignment.getKeyDescription() != null && !assignment.getKeyDescription().isEmpty()) {
            tvKeyDescription.setText(assignment.getKeyDescription());
            tvKeyDescription.setVisibility(View.VISIBLE);
        } else {
            tvKeyDescription.setVisibility(View.GONE);
        }

        // Assignment type
        tvAssignmentType.setText("Type: " + assignment.getAssignmentTypeDisplay());

        // Dates
        tvIssuedDate.setText("Issued: " + formatDate(assignment.getIssuedAt()));

        if (assignment.getExpectedReturn() != null && !assignment.isPermanent()) {
            tvExpectedReturn.setText("Return by: " + formatDate(assignment.getExpectedReturn()));
            tvExpectedReturn.setVisibility(View.VISIBLE);
        } else if (assignment.isPermanent()) {
            tvExpectedReturn.setText("Permanent Assignment");
            tvExpectedReturn.setVisibility(View.VISIBLE);
        } else {
            tvExpectedReturn.setVisibility(View.GONE);
        }

        // Issued by
        if (assignment.getIssuedByName() != null) {
            tvIssuedBy.setText("Issued by: " + assignment.getIssuedByName());
            tvIssuedBy.setVisibility(View.VISIBLE);
        } else {
            tvIssuedBy.setVisibility(View.GONE);
        }

        // Condition
        if (assignment.getConditionOnIssue() != null) {
            tvCondition.setText("Condition: " + assignment.getConditionOnIssue());
            tvCondition.setVisibility(View.VISIBLE);
        } else {
            tvCondition.setVisibility(View.GONE);
        }

        // Notes
        if (assignment.getIssueNotes() != null && !assignment.getIssueNotes().isEmpty()) {
            tvNotes.setText("Notes: " + assignment.getIssueNotes());
            tvNotes.setVisibility(View.VISIBLE);
        } else {
            tvNotes.setVisibility(View.GONE);
        }

        // Status chip
        chipStatus.setText(assignment.getStatusDisplay());
        chipStatus.setChipBackgroundColorResource(assignment.getStatusColor());

        // Deposit information
        if (assignment.getDepositAmount() != null &&
                assignment.getDepositAmount().doubleValue() > 0) {
            cardDeposit.setVisibility(View.VISIBLE);

            StringBuilder depositInfo = new StringBuilder();
            depositInfo.append("Deposit: ").append(assignment.getDepositAmount()).append(" PLN\n");

            if (Boolean.TRUE.equals(assignment.getDepositPaid())) {
                depositInfo.append("✅ Deposit paid");
            } else {
                depositInfo.append("⚠️ Deposit not paid");
            }

            tvDepositInfo.setText(depositInfo.toString());
        } else {
            cardDeposit.setVisibility(View.GONE);
        }

        // Warning card for overdue or lost
        if (assignment.isOverdue() && assignment.isActive()) {
            cardWarning.setVisibility(View.VISIBLE);
            tvWarningText.setText("⚠️ KEY OVERDUE\n\nThis key is overdue by " +
                    assignment.getDaysOverdue() + " days. Please return it as soon as possible.");
        } else if (assignment.isLost()) {
            cardWarning.setVisibility(View.VISIBLE);
            tvWarningText.setText("🔴 KEY LOST\n\nThis key has been reported as lost. " +
                    "Please contact reception.");
        } else {
            cardWarning.setVisibility(View.GONE);
        }

        // Report lost button visibility
        btnReportLost.setVisibility(assignment.isActive() ? View.VISIBLE : View.GONE);
    }

    private void reportKeyLost() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Report Key as Lost?")
                .setMessage("Are you sure you want to report this key as lost? " +
                        "This action cannot be undone and may result in charges.")
                .setPositiveButton("Yes, Report Lost", (dialog, which) -> {
                    performReportLost();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performReportLost() {
        showLoading(true);

        keyRepository.reportKeyLost(assignmentId, new KeyManagementRepository.ReportLostCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);

                    new MaterialAlertDialogBuilder(KeyAssignmentDetailsActivity.this)
                            .setTitle("Key Reported as Lost")
                            .setMessage("The key has been reported as lost. Please contact reception " +
                                    "as soon as possible to arrange for a replacement.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                loadAssignmentDetails(); // Refresh
                            })
                            .setCancelable(false)
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(KeyAssignmentDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String formatDate(String dateStr) {
        if (dateStr == null) return "Unknown";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(
                    "MMM dd, yyyy HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr.substring(0, Math.min(10, dateStr.length()));
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}