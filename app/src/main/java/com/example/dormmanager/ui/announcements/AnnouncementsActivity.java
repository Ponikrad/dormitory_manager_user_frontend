package com.example.dormmanager.ui.announcements;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Announcement;
import com.example.dormmanager.data.repository.AnnouncementRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity implements AnnouncementAdapter.OnAnnouncementClickListener {

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewAnnouncements;
    private AnnouncementAdapter adapter;
    private CircularProgressIndicator progressBar;
    private View layoutEmpty;

    private Chip chipAll, chipPinned, chipUrgent;
    private TextView tvUrgentCount, tvTotalCount;

    private AnnouncementRepository announcementRepository;
    private String currentFilter = "ALL";
    private List<Announcement> allAnnouncements = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();

        announcementRepository = new AnnouncementRepository(this);
        loadAnnouncements();
        loadStatistics();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewAnnouncements = findViewById(R.id.recyclerViewAnnouncements);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        chipAll = findViewById(R.id.chipAll);
        chipPinned = findViewById(R.id.chipPinned);
        chipUrgent = findViewById(R.id.chipUrgent);
        
        tvUrgentCount = findViewById(R.id.tvUrgentCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AnnouncementAdapter(this, this);
        recyclerViewAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnnouncements.setAdapter(adapter);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> filterAnnouncements("ALL"));
        chipPinned.setOnClickListener(v -> filterAnnouncements("PINNED"));
        chipUrgent.setOnClickListener(v -> filterAnnouncements("URGENT"));
    }

    private void filterAnnouncements(String filter) {
        currentFilter = filter;

        if ("ALL".equals(filter)) {
            adapter.setAnnouncements(allAnnouncements);
        } else if ("PINNED".equals(filter)) {
            List<Announcement> pinned = new ArrayList<>();
            for (Announcement announcement : allAnnouncements) {
                if (announcement.isPinned()) {
                    pinned.add(announcement);
                }
            }
            adapter.setAnnouncements(pinned);
        } else if ("URGENT".equals(filter)) {
            List<Announcement> urgent = new ArrayList<>();
            for (Announcement announcement : allAnnouncements) {
                if (announcement.isUrgent()) {
                    urgent.add(announcement);
                }
            }
            adapter.setAnnouncements(urgent);
        }

        updateEmptyState();
    }

    private void loadAnnouncements() {
        showLoading(true);

        announcementRepository.getAnnouncements(new AnnouncementRepository.AnnouncementListCallback() {
            @Override
            public void onSuccess(List<Announcement> announcements) {
                runOnUiThread(() -> {
                    showLoading(false);
                    allAnnouncements = announcements;
                    filterAnnouncements(currentFilter);
                    updateStatistics();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(AnnouncementsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void loadStatistics() {
        // Statistics will be updated from loaded announcements
        // This method can be used if we add a dedicated statistics endpoint
    }

    private void updateStatistics() {
        if (allAnnouncements != null) {
            int total = allAnnouncements.size();
            int urgent = 0;
            for (Announcement announcement : allAnnouncements) {
                if (announcement.isUrgent()) {
                    urgent++;
                }
            }
            
            if (tvTotalCount != null) {
                tvTotalCount.setText(String.valueOf(total));
            }
            if (tvUrgentCount != null) {
                tvUrgentCount.setText(String.valueOf(urgent));
            }
        }
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerViewAnnouncements.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewAnnouncements.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewAnnouncements.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onAnnouncementClick(Announcement announcement) {
        Intent intent = new Intent(this, AnnouncementDetailsActivity.class);
        intent.putExtra("announcement_id", announcement.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAnnouncements();
    }
}