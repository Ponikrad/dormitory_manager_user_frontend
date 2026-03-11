package com.example.dormmanager.ui.issues;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Issue;
import com.example.dormmanager.data.repository.IssueRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IssueDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvIcon;
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvCategory;
    private TextView tvLocation;
    private TextView tvCreatedDate;
    private TextView tvReportedBy;
    private TextView tvAssignedTo;
    private TextView tvAdminNotes;
    private Chip chipStatus;
    private Chip chipPriority;
    private CardView cardRating;
    private CardView cardAssignedTo;
    private CardView cardAdminNotes;
    private RatingBar ratingBar;
    private MaterialButton btnRateResolution;
    private FrameLayout progressBar; // ZMIENIONE z CircularProgressIndicator

    private IssueRepository issueRepository;
    private Issue currentIssue;
    private Long issueId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_details);

        initViews();
        setupToolbar();

        issueRepository = new IssueRepository(this);

        // Get issue ID from intent
        issueId = getIntent().getLongExtra("issue_id", -1);
        if (issueId == -1) {
            Toast.makeText(this, "Invalid issue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadIssueDetails();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvIcon = findViewById(R.id.tvIcon);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvCategory = findViewById(R.id.tvCategory);
        tvLocation = findViewById(R.id.tvLocation);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvReportedBy = findViewById(R.id.tvReportedBy);
        tvAssignedTo = findViewById(R.id.tvAssignedTo);
        tvAdminNotes = findViewById(R.id.tvAdminNotes);
        chipStatus = findViewById(R.id.chipStatus);
        chipPriority = findViewById(R.id.chipPriority);
        cardRating = findViewById(R.id.cardRating);
        cardAssignedTo = findViewById(R.id.cardAssignedTo);
        cardAdminNotes = findViewById(R.id.cardAdminNotes);
        ratingBar = findViewById(R.id.ratingBar);
        btnRateResolution = findViewById(R.id.btnRateResolution);
        progressBar = findViewById(R.id.progressBar); // FrameLayout teraz
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupButtons() {
        btnRateResolution.setOnClickListener(v -> rateResolution());
    }

    private void loadIssueDetails() {
        showLoading(true);

        issueRepository.getIssueById(issueId, new IssueRepository.SingleIssueCallback() {
            @Override
            public void onSuccess(Issue issue) {
                runOnUiThread(() -> {
                    showLoading(false);
                    currentIssue = issue;
                    displayIssueDetails(issue);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(IssueDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayIssueDetails(Issue issue) {
        // Icon
        tvIcon.setText(issue.getIssueIcon());

        // Title and basic info
        tvTitle.setText(issue.getTitle() != null ? issue.getTitle() : "Issue");
        tvDescription.setText(issue.getDescription() != null ? issue.getDescription() : "No description");
        tvCategory.setText(issue.getCategoryDisplay() != null ?
                issue.getCategoryDisplay() : issue.getCategory());
        tvLocation.setText(issue.getLocationDetails() != null ?
                issue.getLocationDetails() : "Not specified");

        // Dates
        String createdDate = formatDate(issue.getReportedAt());
        tvCreatedDate.setText(createdDate);

        // Reporter
        tvReportedBy.setText(issue.getUserFullName() != null ?
                issue.getUserFullName() : "Unknown");

        // Assigned to
        if (issue.getAssignedToUserName() != null) {
            tvAssignedTo.setText(issue.getAssignedToUserName());
            cardAssignedTo.setVisibility(View.VISIBLE);
        } else {
            cardAssignedTo.setVisibility(View.GONE);
        }

        // Admin notes
        if (issue.getAdminNotes() != null && !issue.getAdminNotes().isEmpty()) {
            tvAdminNotes.setText(issue.getAdminNotes());
            cardAdminNotes.setVisibility(View.VISIBLE);
        } else {
            cardAdminNotes.setVisibility(View.GONE);
        }

        // Status chip
        String statusDisplay = issue.getStatusDisplay() != null ?
                issue.getStatusDisplay() : issue.getStatus();
        chipStatus.setText(statusDisplay);
        chipStatus.setChipBackgroundColorResource(getStatusColorResource(issue.getStatus()));

        // Priority chip
        String priorityDisplay = issue.getPriorityDisplay() != null ?
                issue.getPriorityDisplay() : issue.getPriority();
        chipPriority.setText(priorityDisplay);
        chipPriority.setChipBackgroundColorResource(getPriorityColorResource(issue.getPriority()));

        // Rating section
        if (issue.isResolved()) {
            cardRating.setVisibility(View.VISIBLE);

            if (issue.getUserSatisfactionRating() != null && issue.getUserSatisfactionRating() > 0) {
                ratingBar.setRating(issue.getUserSatisfactionRating());
                btnRateResolution.setEnabled(false);
                btnRateResolution.setText("Rated: " + issue.getUserSatisfactionRating() + " ⭐");
            } else {
                btnRateResolution.setEnabled(true);
                btnRateResolution.setText("Submit Rating");
            }
        } else {
            cardRating.setVisibility(View.GONE);
        }

        // Reopen button removed
    }

    private void rateResolution() {
        int rating = Math.round(ratingBar.getRating());

        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Rate Resolution")
                .setMessage("Are you satisfied with how this issue was resolved?\n\nRating: " + rating + " ⭐")
                .setPositiveButton("Submit", (dialog, which) -> {
                    submitRating(rating);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitRating(int rating) {
        showLoading(true);

        issueRepository.rateIssue(issueId, rating, new IssueRepository.RateIssueCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(IssueDetailsActivity.this,
                            "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    btnRateResolution.setEnabled(false);
                    btnRateResolution.setText("Rated: " + rating + " ⭐");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(IssueDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }


    private String formatDate(String dateString) {
        if (dateString == null) return "Unknown";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(
                    "MMM dd, yyyy HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString.substring(0, Math.min(10, dateString.length()));
        }
    }

    private int getStatusColorResource(String status) {
        if (status == null) return R.color.status_pending;

        switch (status.toUpperCase()) {
            case "RESOLVED":
                return R.color.status_completed;
            case "IN_PROGRESS":
            case "ACKNOWLEDGED":
                return R.color.status_pending;
            case "REPORTED":
                return R.color.status_info;
            case "CANCELLED":
            case "CLOSED":
                return R.color.status_failed;
            default:
                return R.color.status_pending;
        }
    }

    private int getPriorityColorResource(String priority) {
        if (priority == null) return R.color.status_pending;

        switch (priority.toUpperCase()) {
            case "CRITICAL":
            case "URGENT":
                return R.color.status_failed;
            case "HIGH":
                return R.color.status_warning;
            case "MEDIUM":
                return R.color.status_info;
            case "LOW":
                return R.color.status_completed;
            default:
                return R.color.status_pending;
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
