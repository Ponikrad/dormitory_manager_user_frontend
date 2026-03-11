package com.example.dormmanager.ui.announcements;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Announcement;
import com.example.dormmanager.data.repository.AnnouncementRepository;
import com.example.dormmanager.utils.DateTimeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class AnnouncementDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvIcon, tvTitle, tvContent, tvAuthor, tvPublishedDate;
    private TextView tvType, tvViewCount;
    private Chip chipPriority, chipUrgent, chipPinned;
    private MaterialButton btnAcknowledge;
    private CircularProgressIndicator progressBar;
    private View cardMetadata;

    private AnnouncementRepository announcementRepository;
    private Announcement currentAnnouncement;
    private Long announcementId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_details);

        initViews();
        setupToolbar();

        announcementRepository = new AnnouncementRepository(this);

        announcementId = getIntent().getLongExtra("announcement_id", -1);
        if (announcementId == -1) {
            Toast.makeText(this, "Invalid announcement", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadAnnouncementDetails();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvIcon = findViewById(R.id.tvIcon);
        tvTitle = findViewById(R.id.tvTitle);
        tvContent = findViewById(R.id.tvContent);
        tvAuthor = findViewById(R.id.tvAuthor);
        tvPublishedDate = findViewById(R.id.tvPublishedDate);
        tvType = findViewById(R.id.tvType);
        tvViewCount = findViewById(R.id.tvViewCount);
        chipPriority = findViewById(R.id.chipPriority);
        chipUrgent = findViewById(R.id.chipUrgent);
        chipPinned = findViewById(R.id.chipPinned);
        btnAcknowledge = findViewById(R.id.btnAcknowledge);
        progressBar = findViewById(R.id.progressBar);
        cardMetadata = findViewById(R.id.cardMetadata);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupButtons() {
        btnAcknowledge.setOnClickListener(v -> acknowledgeAnnouncement());
    }

    private void loadAnnouncementDetails() {
        showLoading(true);

        announcementRepository.getAnnouncementById(announcementId,
                new AnnouncementRepository.SingleAnnouncementCallback() {
                    @Override
                    public void onSuccess(Announcement announcement) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            currentAnnouncement = announcement;
                            displayAnnouncementDetails(announcement);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(AnnouncementDetailsActivity.this,
                                    "Error: " + error, Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
    }

    private void displayAnnouncementDetails(Announcement announcement) {
        tvIcon.setText(announcement.getAnnouncementIcon());

        tvTitle.setText(announcement.getTitle() != null ?
                announcement.getTitle() : "Announcement");

        tvContent.setText(announcement.getContent() != null ?
                announcement.getContent() : "");

        if (announcement.getAuthorName() != null) {
            tvAuthor.setText("Posted by: " + announcement.getAuthorName());
            tvAuthor.setVisibility(View.VISIBLE);
        } else {
            tvAuthor.setVisibility(View.GONE);
        }

        tvPublishedDate.setText("Published: " +
                DateTimeUtils.formatDateTime(announcement.getPublishedAt()));

        tvType.setText("Category: " + announcement.getTypeDisplay());

        if (announcement.getViewCount() != null) {
            tvViewCount.setText("👁 " + announcement.getViewCount() + " views");
            tvViewCount.setVisibility(View.VISIBLE);
        } else {
            tvViewCount.setVisibility(View.GONE);
        }

        if (announcement.getPriority() != null) {
            chipPriority.setVisibility(View.VISIBLE);
            chipPriority.setText(announcement.getPriorityIcon() + " " +
                    announcement.getPriority());
            chipPriority.setChipBackgroundColorResource(announcement.getPriorityColor());
        } else {
            chipPriority.setVisibility(View.GONE);
        }

        chipUrgent.setVisibility(announcement.isUrgent() ? View.VISIBLE : View.GONE);

        chipPinned.setVisibility(announcement.isPinned() ? View.VISIBLE : View.GONE);

        if (announcement.isUrgent() || "CRITICAL".equals(announcement.getPriority()) ||
                "HIGH".equals(announcement.getPriority())) {
            btnAcknowledge.setVisibility(View.VISIBLE);
        } else {
            btnAcknowledge.setVisibility(View.GONE);
        }
    }

    private void acknowledgeAnnouncement() {
        showLoading(true);

        announcementRepository.acknowledgeAnnouncement(announcementId,
                new AnnouncementRepository.AcknowledgeCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            showLoading(false);
                            btnAcknowledge.setEnabled(false);
                            btnAcknowledge.setText("✓ Acknowledged");
                            Toast.makeText(AnnouncementDetailsActivity.this,
                                    "Announcement acknowledged", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            Toast.makeText(AnnouncementDetailsActivity.this,
                                    "Error: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}