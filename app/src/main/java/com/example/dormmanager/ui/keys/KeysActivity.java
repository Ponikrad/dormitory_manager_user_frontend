package com.example.dormmanager.ui.keys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.KeyAssignment;
import com.example.dormmanager.data.repository.KeyManagementRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class KeysActivity extends AppCompatActivity implements KeyAssignmentAdapter.OnKeyAssignmentClickListener {

    private static final int ITEMS_PER_PAGE = 6;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewKeys;
    private KeyAssignmentAdapter adapter;
    private FrameLayout progressBar;
    private LinearLayout layoutEmpty;
    private LinearLayout paginationContainer;
    private TextView tvActiveCount, tvTotalCount, tvPageInfo;
    private MaterialButton btnPrevPage, btnNextPage, btnReportLost;

    private Chip chipAll, chipActive, chipReturned;

    private KeyManagementRepository keyRepository;
    private String currentFilter = "ALL";
    private int currentPage = 0;
    private int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keys);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();

        keyRepository = new KeyManagementRepository(this);
        loadKeyAssignments();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewKeys = findViewById(R.id.recyclerViewKeys);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvActiveCount = findViewById(R.id.tvActiveCount);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        paginationContainer = findViewById(R.id.paginationContainer);
        tvPageInfo = findViewById(R.id.tvPageInfo);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);

        chipAll = findViewById(R.id.chipAll);
        chipActive = findViewById(R.id.chipActive);
        chipReturned = findViewById(R.id.chipReturned);
        btnReportLost = findViewById(R.id.btnReportLost);

        btnReportLost.setOnClickListener(v -> {
            // Show report lost key dialog
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Report Lost Key")
                    .setMessage("If you've lost a key, please contact the administration immediately.\n\nA fee may be charged for lost keys.")
                    .setPositiveButton("Contact Admin", (dialog, which) -> {
                        // Open messages to send to admin
                        Intent intent = new Intent(this, com.example.dormmanager.ui.messages.SendMessageActivity.class);
                        intent.putExtra("subject", "Lost Key Report");
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
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

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new KeyAssignmentAdapter(this, this);
        recyclerViewKeys.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewKeys.setAdapter(adapter);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> filterKeys("ALL"));
        chipActive.setOnClickListener(v -> filterKeys("ACTIVE"));
        chipReturned.setOnClickListener(v -> filterKeys("RETURNED"));
    }

    private void filterKeys(String filter) {
        currentFilter = filter;
        currentPage = 0;
        adapter.filter(filter);
        displayCurrentPage();
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
        
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerViewKeys.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewKeys.setVisibility(View.VISIBLE);
        }
    }

    private void loadKeyAssignments() {
        showLoading(true);

        keyRepository.getMyAssignments(new KeyManagementRepository.KeyAssignmentListCallback() {
            @Override
            public void onSuccess(List<KeyAssignment> assignments) {
                runOnUiThread(() -> {
                    showLoading(false);
                    adapter.setKeyAssignments(assignments);
                    filterKeys(currentFilter);
                    updateSummary(assignments);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(KeysActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void updateSummary(List<KeyAssignment> assignments) {
        int activeCount = 0;

        for (KeyAssignment assignment : assignments) {
            if (assignment.isActive()) {
                activeCount++;
            }
        }

        tvActiveCount.setText(String.valueOf(activeCount));
        tvTotalCount.setText(String.valueOf(assignments.size()));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewKeys.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onKeyAssignmentClick(KeyAssignment assignment) {
        Intent intent = new Intent(this, KeyAssignmentDetailsActivity.class);
        intent.putExtra("assignment_id", assignment.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh keys when returning
        loadKeyAssignments();
    }
}