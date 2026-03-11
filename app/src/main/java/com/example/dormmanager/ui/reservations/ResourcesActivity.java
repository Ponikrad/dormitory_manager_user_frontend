package com.example.dormmanager.ui.reservations;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.ReservableResource;
import com.example.dormmanager.data.repository.ReservationRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.List;

public class ResourcesActivity extends AppCompatActivity implements ResourceAdapter.OnResourceClickListener {

    private MaterialToolbar toolbar;
    private SearchView searchView;
    private RecyclerView recyclerViewResources;
    private ResourceAdapter adapter;
    private CircularProgressIndicator progressBar;
    private View layoutEmpty;

    private Chip chipAll, chipLaundry, chipGameRoom, chipStudyRoom, chipKitchen, chipGym;

    private ReservationRepository reservationRepository;
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupFilters();

        reservationRepository = new ReservationRepository(this);
        loadResources();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchView = findViewById(R.id.searchView);
        recyclerViewResources = findViewById(R.id.recyclerViewResources);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        chipAll = findViewById(R.id.chipAll);
        chipLaundry = findViewById(R.id.chipLaundry);
        chipGameRoom = findViewById(R.id.chipGameRoom);
        chipStudyRoom = findViewById(R.id.chipStudyRoom);
        chipKitchen = findViewById(R.id.chipKitchen);
        chipGym = findViewById(R.id.chipGym);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ResourceAdapter(this, this);
        recyclerViewResources.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewResources.setAdapter(adapter);
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.search(newText);
                return true;
            }
        });
    }

    private void setupFilters() {
        chipAll.setOnClickListener(v -> filterResources("ALL"));
        chipLaundry.setOnClickListener(v -> filterResources("LAUNDRY"));
        chipGameRoom.setOnClickListener(v -> filterResources("GAME_ROOM"));
        chipStudyRoom.setOnClickListener(v -> filterResources("STUDY_ROOM"));
        chipKitchen.setOnClickListener(v -> filterResources("KITCHEN"));
        chipGym.setOnClickListener(v -> filterResources("GYM"));
    }

    private void filterResources(String filter) {
        currentFilter = filter;
        adapter.filter(filter);

        // Update empty state
        updateEmptyState();
    }

    private void loadResources() {
        showLoading(true);

        reservationRepository.getAllResources(new ReservationRepository.ResourceListCallback() {
            @Override
            public void onSuccess(List<ReservableResource> resources) {
                runOnUiThread(() -> {
                    showLoading(false);
                    adapter.setResources(resources);
                    filterResources(currentFilter);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ResourcesActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void updateEmptyState() {
        if (adapter.getItemCount() == 0) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerViewResources.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            recyclerViewResources.setVisibility(View.VISIBLE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerViewResources.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onResourceClick(ReservableResource resource) {
        // Return selected resource to CreateReservationActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selected_resource_id", resource.getId());
        resultIntent.putExtra("selected_resource_name", resource.getName());
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}