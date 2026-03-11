package com.example.dormmanager.ui.issues;

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
import com.example.dormmanager.data.local.TokenManager;
import com.example.dormmanager.data.model.Issue;
import com.example.dormmanager.data.repository.IssueRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class IssuesActivity extends AppCompatActivity implements IssueAdapter.OnIssueClickListener {

    private static final int ITEMS_PER_PAGE = 6;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewIssues;
    private IssueAdapter adapter;
    private FrameLayout progressBar;
    private View layoutEmpty;
    private LinearLayout paginationContainer;
    private TextView tvOpenCount;
    private TextView tvResolvedCount;
    private TextView tvPageInfo;
    private MaterialButton btnPrevPage, btnNextPage, btnReportIssue;

    private Chip chipAll, chipOpen, chipResolved;

    private IssueRepository issueRepository;
    private String currentFilter = "ALL";
    private int currentPage = 0;
    private int totalPages = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issues);

        // Check if user is receptionist (they can't access personal issues)
        TokenManager tokenManager = new TokenManager(this);
        String role = tokenManager.getRole();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        setupFab();

        issueRepository = new IssueRepository(this);
        loadIssues();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewIssues = findViewById(R.id.recyclerViewIssues);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvOpenCount = findViewById(R.id.tvOpenCount);
        tvResolvedCount = findViewById(R.id.tvResolvedCount);
        btnReportIssue = findViewById(R.id.btnReportIssue);
        paginationContainer = findViewById(R.id.paginationContainer);
        tvPageInfo = findViewById(R.id.tvPageInfo);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);

        chipAll = findViewById(R.id.chipAll);
        chipOpen = findViewById(R.id.chipOpen);
        chipResolved = findViewById(R.id.chipResolved);

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new IssueAdapter(this, this);
        recyclerViewIssues.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewIssues.setAdapter(adapter);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> filterIssues("ALL"));
        chipOpen.setOnClickListener(v -> filterIssues("REPORTED"));
        chipResolved.setOnClickListener(v -> filterIssues("RESOLVED"));
    }

    private void setupFab() {
        btnReportIssue.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateIssueActivity.class);
            startActivity(intent);
        });
    }

    private void filterIssues(String status) {
        currentFilter = status;
        currentPage = 0;
        adapter.filter(status);
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
            recyclerViewIssues.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewIssues.setVisibility(View.VISIBLE);
        }
    }

    private void loadIssues() {
        showLoading(true);

        issueRepository.getMyIssues(0, 100, new IssueRepository.IssueCallback() {
            @Override
            public void onSuccess(List<Issue> issues) {
                runOnUiThread(() -> {
                    showLoading(false);
                    adapter.setIssues(issues);
                    filterIssues(currentFilter);
                    updateSummary(issues);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(IssuesActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void updateSummary(List<Issue> issues) {
        int openCount = 0;
        int resolvedCount = 0;

        for (Issue issue : issues) {
            if (issue.isOpen()) {
                openCount++;
            } else if (issue.isResolved()) {
                resolvedCount++;
            }
        }

        tvOpenCount.setText(String.valueOf(openCount));
        tvResolvedCount.setText(String.valueOf(resolvedCount));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewIssues.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onIssueClick(Issue issue) {
        Intent intent = new Intent(this, IssueDetailsActivity.class);
        intent.putExtra("issue_id", issue.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh issues when returning from other activities
        loadIssues();
    }
}