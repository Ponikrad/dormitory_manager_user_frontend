package com.example.dormmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.data.local.TokenManager;
import com.example.dormmanager.ui.announcements.AnnouncementsActivity;
import com.example.dormmanager.ui.auth.LoginActivity;
import com.example.dormmanager.ui.auth.ProfileActivity;
import com.example.dormmanager.ui.payments.PaymentsActivity;
import com.example.dormmanager.ui.issues.IssuesActivity;
import com.example.dormmanager.ui.reservations.ReservationsActivity;
import com.example.dormmanager.ui.keys.KeysActivity;
import com.example.dormmanager.utils.ThemeManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.example.dormmanager.ui.messages.MessagesActivity;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvWelcome, tvAnnouncementsSummary;
    private MaterialCardView cardPayments, cardIssues, cardReservations;
    private MaterialCardView cardMessages, cardKeys, cardProfile, cardAnnouncements;

    private TokenManager tokenManager;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Zastosuj zapisany motyw przed setContentView
        themeManager = new ThemeManager(this);
        themeManager.applySavedTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);

        // Check if user is logged in
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupToolbar();
        setupDashboardCards();
        displayWelcomeMessage();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvAnnouncementsSummary = findViewById(R.id.tvAnnouncementsSummary);

        cardAnnouncements = findViewById(R.id.cardAnnouncements);
        cardPayments = findViewById(R.id.cardPayments);
        cardIssues = findViewById(R.id.cardIssues);
        cardReservations = findViewById(R.id.cardReservations);
        cardMessages = findViewById(R.id.cardMessages);
        cardKeys = findViewById(R.id.cardKeys);
        cardProfile = findViewById(R.id.cardProfile);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Dorm Manager");
        }
    }

    private void setupDashboardCards() {
        String userRole = tokenManager.getRole();
        boolean isAdmin = "ADMIN".equals(userRole);
        boolean isReceptionist = "RECEPTIONIST".equals(userRole);

        // Announcements - Everyone
        cardAnnouncements.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnnouncementsActivity.class);
            startActivity(intent);
        });

        // Profile - Everyone
        cardProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        // Messages - Everyone
        cardMessages.setOnClickListener(v -> {
            Intent intent = new Intent(this, MessagesActivity.class);
            startActivity(intent);
        });

        // Reservations - Everyone
        cardReservations.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReservationsActivity.class);
            startActivity(intent);
        });

        // Issues - Everyone
        cardIssues.setOnClickListener(v -> {
            Intent intent = new Intent(this, IssuesActivity.class);
            startActivity(intent);
        });

        // Payments - Students and Admins can access
        cardPayments.setOnClickListener(v -> {
            if (isReceptionist) {
                Toast.makeText(this,
                        "Receptionist cannot access payments",
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, PaymentsActivity.class);
                startActivity(intent);
            }
        });

        // Keys - Everyone
        cardKeys.setOnClickListener(v -> {
            Intent intent = new Intent(this, KeysActivity.class);
            startActivity(intent);
        });
    }

    private void displayWelcomeMessage() {
        String fullName = tokenManager.getFullName();

        String welcomeText = "Welcome!";
        if (fullName != null) {
            welcomeText = "Welcome, " + fullName + "! 🎉";
        }
        tvWelcome.setText(welcomeText);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem themeItem = menu.findItem(R.id.action_theme_toggle);
        if (themeItem != null) {
            if (themeManager.isDarkMode()) {
                themeItem.setIcon(R.drawable.ic_light_mode);
                themeItem.setTitle("Switch to Light Mode");
            } else {
                themeItem.setIcon(R.drawable.ic_dark_mode);
                themeItem.setTitle("Switch to Dark Mode");
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme_toggle) {
            themeManager.toggleTheme();
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayWelcomeMessage();
    }
}