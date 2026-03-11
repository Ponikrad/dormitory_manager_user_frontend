package com.example.dormmanager.ui.payments;

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
import com.example.dormmanager.data.model.Payment;
import com.example.dormmanager.data.repository.PaymentRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class PaymentsActivity extends AppCompatActivity implements PaymentAdapter.OnPaymentClickListener {

    private static final int ITEMS_PER_PAGE = 6;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewPayments;
    private PaymentAdapter adapter;
    private FrameLayout progressBar;
    private View layoutEmpty;
    private LinearLayout paginationContainer;
    private TextView tvTotalPaid;
    private TextView tvTotalPending;
    private TextView tvPageInfo;
    private MaterialButton btnPrevPage, btnNextPage, btnNewPayment;

    private Chip chipAll, chipPending, chipCompleted;

    private PaymentRepository paymentRepository;
    private String currentFilter = "ALL";
    private int currentPage = 0;
    private int totalPages = 1;
    private List<Payment> allPayments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        // Check if user is receptionist
        TokenManager tokenManager = new TokenManager(this);
        String role = tokenManager.getRole();

        if ("RECEPTIONIST".equals(role)) {
            Toast.makeText(this,
                    "This feature is not available for receptionists",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        setupFab();

        paymentRepository = new PaymentRepository(this);
        loadPayments();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewPayments = findViewById(R.id.recyclerViewPayments);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvTotalPaid = findViewById(R.id.tvTotalPaid);
        tvTotalPending = findViewById(R.id.tvTotalPending);
        btnNewPayment = findViewById(R.id.btnNewPayment);
        paginationContainer = findViewById(R.id.paginationContainer);
        tvPageInfo = findViewById(R.id.tvPageInfo);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);

        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipCompleted = findViewById(R.id.chipCompleted);

        // Setup pagination buttons
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
        adapter = new PaymentAdapter(this, this);
        recyclerViewPayments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPayments.setAdapter(adapter);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> filterPayments("ALL"));
        chipPending.setOnClickListener(v -> filterPayments("PENDING"));
        chipCompleted.setOnClickListener(v -> filterPayments("COMPLETED"));
    }

    private void setupFab() {
        btnNewPayment.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreatePaymentActivity.class);
            startActivity(intent);
        });
    }

    private void filterPayments(String status) {
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
            recyclerViewPayments.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewPayments.setVisibility(View.VISIBLE);
        }
    }

    private void loadPayments() {
        showLoading(true);

        paymentRepository.getMyPayments(0, 500, new PaymentRepository.PaymentCallback() {
            @Override
            public void onSuccess(List<Payment> payments) {
                runOnUiThread(() -> {
                    showLoading(false);
                    allPayments = payments;
                    adapter.setPayments(payments);
                    filterPayments(currentFilter);
                    updateSummary(payments);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(PaymentsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void updateSummary(List<Payment> payments) {
        double totalPaid = 0;
        double totalPending = 0;

        for (Payment payment : payments) {
            if (payment.getAmount() != null) {
                if ("COMPLETED".equals(payment.getStatus())) {
                    totalPaid += payment.getAmount();
                } else if ("PENDING".equals(payment.getStatus()) ||
                        "PROCESSING".equals(payment.getStatus())) {
                    totalPending += payment.getAmount();
                }
            }
        }

        tvTotalPaid.setText(String.format("%.2f PLN", totalPaid));
        tvTotalPending.setText(String.format("%.2f PLN", totalPending));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewPayments.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onPaymentClick(Payment payment) {
        Intent intent = new Intent(this, PaymentDetailsActivity.class);
        intent.putExtra("payment_id", payment.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPayments();
    }
}