package com.example.dormmanager.ui.reservations;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Reservation;
import com.example.dormmanager.data.repository.ReservationRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReservationDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ReservationDetails";

    private MaterialToolbar toolbar;
    private TextView tvIcon, tvTitle, tvResourceType, tvLocation, tvDescription;
    private TextView tvStartTime, tvEndTime, tvDuration, tvPeople;
    private TextView tvCreatedDate, tvStatus, tvKeyInfo, tvNotes;
    private Chip chipStatus;
    private MaterialButton btnCancel, btnCheckIn, btnPickUpKey;
    private FrameLayout progressBar;

    private ReservationRepository reservationRepository;
    private Reservation currentReservation;
    private Long reservationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_details);

        initViews();
        setupToolbar();

        reservationRepository = new ReservationRepository(this);

        // Get reservation ID from intent
        reservationId = getIntent().getLongExtra("reservation_id", -1);
        if (reservationId == -1) {
            Toast.makeText(this, "Invalid reservation", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadReservationDetails();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvIcon = findViewById(R.id.tvIcon);
        tvTitle = findViewById(R.id.tvTitle);
        tvResourceType = findViewById(R.id.tvResourceType);
        tvLocation = findViewById(R.id.tvLocation);
        tvDescription = findViewById(R.id.tvDescription);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        tvDuration = findViewById(R.id.tvDuration);
        tvPeople = findViewById(R.id.tvPeople);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvKeyInfo = findViewById(R.id.tvKeyInfo);
        tvNotes = findViewById(R.id.tvNotes);
        chipStatus = findViewById(R.id.chipStatus);
        btnCancel = findViewById(R.id.btnCancel);
        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnPickUpKey = findViewById(R.id.btnPickUpKey);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupButtons() {
        btnCancel.setOnClickListener(v -> showCancelDialog());
        btnCheckIn.setOnClickListener(v -> checkInReservation());
        btnPickUpKey.setOnClickListener(v -> showKeyPickupInfo());
    }

    private void loadReservationDetails() {
        showLoading(true);

        reservationRepository.getReservationById(reservationId, new ReservationRepository.SingleReservationCallback() {
            @Override
            public void onSuccess(Reservation reservation) {
                runOnUiThread(() -> {
                    showLoading(false);
                    currentReservation = reservation;
                    displayReservationDetails(reservation);
                    updateButtonVisibility(reservation);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ReservationDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayReservationDetails(Reservation reservation) {
        Log.d(TAG, "Displaying reservation: " + reservation.toString());
        Log.d(TAG, "canCancel: " + reservation.getCanCancel());
        Log.d(TAG, "canCheckIn: " + reservation.getCanCheckIn());
        Log.d(TAG, "canPickUpKey: " + reservation.getCanPickUpKey());

        // Icon and Title
        tvIcon.setText(reservation.getReservationIcon());
        tvTitle.setText(reservation.getResourceName() != null ?
                reservation.getResourceName() : "Reservation");

        // Resource Type
        tvResourceType.setText(reservation.getResourceType() != null ?
                reservation.getResourceType().replace("_", " ") : "");

        // Location
        tvLocation.setText(reservation.getResourceLocation() != null ?
                reservation.getResourceLocation() : "Location not specified");

        // Description
        if (reservation.getResourceType() != null) {
            tvDescription.setText(getResourceDescription(reservation.getResourceType()));
        }

        // Time details
        tvStartTime.setText(formatDateTime(reservation.getStartTime()));
        tvEndTime.setText(formatDateTime(reservation.getEndTime()));

        if (reservation.getFormattedDuration() != null) {
            tvDuration.setText(reservation.getFormattedDuration());
        } else if (reservation.getDurationMinutes() != null) {
            tvDuration.setText(reservation.getDurationMinutes() + " minutes");
        }

        // People
        int numPeople = reservation.getNumberOfPeople() != null ?
                reservation.getNumberOfPeople() : 1;
        tvPeople.setText(numPeople + (numPeople == 1 ? " person" : " people"));

        // Created date
        tvCreatedDate.setText(formatDate(reservation.getCreatedAt()));

        // Status
        String statusDisplay = reservation.getStatusDisplay() != null ?
                reservation.getStatusDisplay() : reservation.getStatus();
        tvStatus.setText(statusDisplay);
        chipStatus.setText(statusDisplay);
        chipStatus.setChipBackgroundColorResource(getStatusColor(reservation.getStatus()));

        // Key information
        View keyInfoCard = tvKeyInfo.getParent().getParent() instanceof View ?
                (View) tvKeyInfo.getParent().getParent() : null;

        if (Boolean.TRUE.equals(reservation.getRequiresKey())) {
            if (keyInfoCard != null) {
                keyInfoCard.setVisibility(View.VISIBLE);
            }

            StringBuilder keyInfo = new StringBuilder();
            if (Boolean.TRUE.equals(reservation.getKeyPickedUp())) {
                keyInfo.append("✅ Key picked up\n");
                if (reservation.getKeyPickedUpAt() != null) {
                    keyInfo.append("Picked up: ").append(formatDateTime(reservation.getKeyPickedUpAt()));
                }
                if (Boolean.TRUE.equals(reservation.getKeyReturned())) {
                    keyInfo.append("\n✅ Key returned");
                } else {
                    keyInfo.append("\n⚠️ Remember to return the key!");
                }
            } else {
                keyInfo.append("🔑 Pick up at: ").append(reservation.getKeyLocation());
            }

            tvKeyInfo.setText(keyInfo.toString());
        } else {
            if (keyInfoCard != null) {
                keyInfoCard.setVisibility(View.GONE);
            }
        }

        // Notes
        View notesCard = tvNotes.getParent().getParent() instanceof View ?
                (View) tvNotes.getParent().getParent() : null;

        if (reservation.getNotes() != null && !reservation.getNotes().isEmpty()) {
            if (notesCard != null) {
                notesCard.setVisibility(View.VISIBLE);
            }
            tvNotes.setText(reservation.getNotes());
        } else {
            if (notesCard != null) {
                notesCard.setVisibility(View.GONE);
            }
        }
    }

    private void updateButtonVisibility(Reservation reservation) {
        // Cancel button
        boolean canCancel = Boolean.TRUE.equals(reservation.getCanCancel());
        Log.d(TAG, "Setting Cancel button visibility: " + canCancel);
        btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);

        // Check-in button
        boolean canCheckIn = Boolean.TRUE.equals(reservation.getCanCheckIn());
        Log.d(TAG, "Setting CheckIn button visibility: " + canCheckIn);
        btnCheckIn.setVisibility(canCheckIn ? View.VISIBLE : View.GONE);

        // Pick up key button
        boolean canPickUpKey = Boolean.TRUE.equals(reservation.getCanPickUpKey());
        Log.d(TAG, "Setting PickUpKey button visibility: " + canPickUpKey);
        btnPickUpKey.setVisibility(canPickUpKey ? View.VISIBLE : View.GONE);

        // Log status info for debugging
        Log.d(TAG, "Reservation status: " + reservation.getStatus());
        Log.d(TAG, "Start time: " + reservation.getStartTime());
        Log.d(TAG, "End time: " + reservation.getEndTime());
        Log.d(TAG, "Minutes until start: " + reservation.getMinutesUntilStart());
    }

    private void showCancelDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cancel Reservation?")
                .setMessage("Are you sure you want to cancel this reservation?\n\nThis action cannot be undone.")
                .setIcon(R.drawable.ic_issues)
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    performCancel();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performCancel() {
        showLoading(true);

        reservationRepository.cancelReservation(reservationId, new ReservationRepository.CancelReservationCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);

                    new MaterialAlertDialogBuilder(ReservationDetailsActivity.this)
                            .setTitle("Reservation Cancelled")
                            .setMessage("Your reservation has been cancelled successfully.")
                            .setPositiveButton("OK", (dialog, which) -> {
                                finish(); // Return to previous screen
                            })
                            .setCancelable(false)
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);

                    new MaterialAlertDialogBuilder(ReservationDetailsActivity.this)
                            .setTitle("Cannot Cancel")
                            .setMessage(parseErrorMessage(error))
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }

    private void checkInReservation() {
        showLoading(true);

        reservationRepository.checkInReservation(reservationId, new ReservationRepository.CheckInCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);

                    new MaterialAlertDialogBuilder(ReservationDetailsActivity.this)
                            .setTitle("Checked In! ✅")
                            .setMessage("You have successfully checked in to your reservation.\n\nEnjoy your time!")
                            .setPositiveButton("OK", (dialog, which) -> {
                                loadReservationDetails(); // Refresh to update status
                            })
                            .setCancelable(false)
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);

                    new MaterialAlertDialogBuilder(ReservationDetailsActivity.this)
                            .setTitle("Cannot Check In")
                            .setMessage(parseErrorMessage(error))
                            .setPositiveButton("OK", null)
                            .show();
                });
            }
        });
    }

    private void showKeyPickupInfo() {
        if (currentReservation == null) return;

        String message = "Go to " + currentReservation.getKeyLocation() +
                " to pick up the key.\n\nShow this reservation to the receptionist.";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Key Pickup Information")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setIcon(R.drawable.ic_key)
                .show();
    }

    private String parseErrorMessage(String error) {
        if (error == null || error.isEmpty()) {
            return "An unknown error occurred. Please try again.";
        }

        // Check for specific error messages
        if (error.toLowerCase().contains("too late") || error.toLowerCase().contains("past")) {
            return "⚠️ Too Late\n\nThe check-in window has passed. Please contact administration for assistance.";
        }

        if (error.toLowerCase().contains("too early")) {
            return "⚠️ Too Early\n\nYou can only check in 15 minutes before your reservation starts.";
        }

        if (error.toLowerCase().contains("not found")) {
            return "⚠️ Reservation Not Found\n\nThis reservation no longer exists.";
        }

        if (error.toLowerCase().contains("already cancelled")) {
            return "⚠️ Already Cancelled\n\nThis reservation has already been cancelled.";
        }

        if (error.toLowerCase().contains("cannot cancel")) {
            return "⚠️ Cannot Cancel\n\nReservations must be cancelled at least 1 hour in advance.";
        }

        return "⚠️ Error\n\n" + error;
    }

    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null) return "Unknown";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateTimeStr;
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null) return "Unknown";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr.substring(0, Math.min(10, dateStr.length()));
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return R.color.status_pending;

        switch (status.toUpperCase()) {
            case "CONFIRMED":
                return R.color.status_completed;
            case "CHECKED_IN":
                return R.color.primary_blue;
            case "COMPLETED":
                return R.color.status_completed;
            case "CANCELLED":
            case "REJECTED":
                return R.color.status_failed;
            default:
                return R.color.status_pending;
        }
    }

    private String getResourceDescription(String resourceType) {
        switch (resourceType.toUpperCase()) {
            case "LAUNDRY":
                return "Washing machines and dryers available";
            case "GAME_ROOM":
                return "Gaming consoles, board games, and entertainment";
            case "STUDY_ROOM":
                return "Quiet space for studying and learning";
            case "KITCHEN":
                return "Cooking facilities and equipment";
            case "GYM":
                return "Fitness equipment and exercise space";
            case "CONFERENCE_ROOM":
                return "Meeting and conference facilities";
            default:
                return "Facility available for your use";
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh when returning to screen
        if (reservationId != null && reservationId != -1) {
            loadReservationDetails();
        }
    }
}