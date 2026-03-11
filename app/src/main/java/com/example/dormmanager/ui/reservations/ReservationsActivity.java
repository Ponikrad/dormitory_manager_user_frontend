package com.example.dormmanager.ui.reservations;

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
import com.example.dormmanager.data.model.Reservation;
import com.example.dormmanager.data.repository.ReservationRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

public class ReservationsActivity extends AppCompatActivity
        implements ReservationAdapter.OnReservationClickListener {

    private static final int ITEMS_PER_PAGE = 6;

    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewReservations;
    private ReservationAdapter reservationAdapter;
    private FrameLayout progressBar;
    private LinearLayout layoutEmpty;
    private LinearLayout paginationContainer;
    private TextView tvActiveCount;
    private TextView tvPastCount;
    private TextView tvPageInfo;
    private MaterialButton btnPrevPage, btnNextPage, btnNewReservation;

    private Chip chipAll, chipUpcoming, chipActive, chipPast;

    private ReservationRepository reservationRepository;
    private String currentFilter = "ALL";
    private int currentPage = 0;
    private int totalPages = 1;
    private List<Reservation> allReservations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservations);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupFilters();
        setupFab();

        reservationRepository = new ReservationRepository(this);
        loadReservations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewReservations = findViewById(R.id.recyclerViewReservations);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvActiveCount = findViewById(R.id.tvActiveCount);
        tvPastCount = findViewById(R.id.tvPastCount);
        btnNewReservation = findViewById(R.id.btnNewReservation);
        paginationContainer = findViewById(R.id.paginationContainer);
        tvPageInfo = findViewById(R.id.tvPageInfo);
        btnPrevPage = findViewById(R.id.btnPrevPage);
        btnNextPage = findViewById(R.id.btnNextPage);

        chipAll = findViewById(R.id.chipAll);
        chipUpcoming = findViewById(R.id.chipUpcoming);
        chipActive = findViewById(R.id.chipActive);
        chipPast = findViewById(R.id.chipPast);

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
        reservationAdapter = new ReservationAdapter(this, this);
        recyclerViewReservations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReservations.setAdapter(reservationAdapter);
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> filterReservations("ALL"));
        chipUpcoming.setOnClickListener(v -> filterReservations("UPCOMING"));
        chipActive.setOnClickListener(v -> filterReservations("ACTIVE"));
        chipPast.setOnClickListener(v -> filterReservations("PAST"));
    }

    private void setupFab() {
        btnNewReservation.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateReservationActivity.class);
            startActivity(intent);
        });
    }

    private void filterReservations(String filter) {
        currentFilter = filter;
        currentPage = 0; // Reset to first page when filter changes
        
        // Filter all reservations
        List<Reservation> filteredList = new ArrayList<>();
        for (Reservation reservation : allReservations) {
            boolean matches = false;
            
            if (filter == null || filter.equals("ALL")) {
                matches = true;
            } else {
                switch (filter.toUpperCase()) {
                    case "UPCOMING":
                        matches = reservation.isUpcoming();
                        break;
                    case "ACTIVE":
                        matches = reservation.isActive();
                        break;
                    case "PAST":
                        matches = "COMPLETED".equalsIgnoreCase(reservation.getStatus()) ||
                                "CANCELLED".equalsIgnoreCase(reservation.getStatus());
                        break;
                }
            }
            
            if (matches) {
                filteredList.add(reservation);
            }
        }
        
        reservationAdapter.setReservations(filteredList);
        displayCurrentPage();
    }

    private void displayCurrentPage() {
        int totalItems = reservationAdapter.getTotalItemCount();
        totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        
        // Ensure currentPage is valid
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        if (currentPage < 0) {
            currentPage = 0;
        }
        
        reservationAdapter.setPage(currentPage, ITEMS_PER_PAGE);
        
        // Update pagination UI
        if (totalItems > ITEMS_PER_PAGE) {
            paginationContainer.setVisibility(View.VISIBLE);
            tvPageInfo.setText("Page " + (currentPage + 1) + " of " + totalPages);
            btnPrevPage.setEnabled(currentPage > 0);
            btnNextPage.setEnabled(currentPage < totalPages - 1);
        } else {
            paginationContainer.setVisibility(View.GONE);
        }
        
        // Update empty state
        if (reservationAdapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerViewReservations.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewReservations.setVisibility(View.VISIBLE);
        }
    }

    private void loadReservations() {
        showLoading(true);

        reservationRepository.getMyReservations(new ReservationRepository.ReservationListCallback() {
            @Override
            public void onSuccess(List<Reservation> reservations) {
                runOnUiThread(() -> {
                    showLoading(false);
                    allReservations = reservations;
                    filterReservations(currentFilter);
                    updateSummary(reservations);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ReservationsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void updateSummary(List<Reservation> reservations) {
        int activeCount = 0;
        int pastCount = 0;

        for (Reservation reservation : reservations) {
            if (reservation.isActive() || reservation.isUpcoming()) {
                activeCount++;
            } else if ("COMPLETED".equalsIgnoreCase(reservation.getStatus()) ||
                    "CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
                pastCount++;
            }
        }

        tvActiveCount.setText(String.valueOf(activeCount));
        tvPastCount.setText(String.valueOf(pastCount));
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewReservations.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onReservationClick(Reservation reservation) {
        Intent intent = new Intent(this, ReservationDetailsActivity.class);
        intent.putExtra("reservation_id", reservation.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh reservations when returning
        loadReservations();
    }
}