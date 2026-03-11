package com.example.dormmanager.ui.reservations;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.CreateReservationRequest;
import com.example.dormmanager.data.model.Reservation;
import com.example.dormmanager.data.model.ReservableResource;
import com.example.dormmanager.data.repository.ReservationRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CreateReservationActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilResource, tilStartDate, tilStartTime, tilEndDate, tilEndTime, tilPeople;
    private AutoCompleteTextView actvResource;
    private TextInputEditText etStartDate, etStartTime, etEndDate, etEndTime, etPeople, etNotes;
    private MaterialButton btnSelectResource, btnCreateReservation;
    private View loadingOverlay;

    private ReservationRepository reservationRepository;
    private List<ReservableResource> availableResources = new ArrayList<>();
    private ReservableResource selectedResource;

    private Calendar startDateTime = Calendar.getInstance();
    private Calendar endDateTime = Calendar.getInstance();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_reservation);

        initViews();
        setupToolbar();
        setupDateTimePickers();
        setupButtons();

        reservationRepository = new ReservationRepository(this);

        // Set default times (next hour, +2 hours)
        startDateTime.add(Calendar.HOUR_OF_DAY, 1);
        startDateTime.set(Calendar.MINUTE, 0);
        endDateTime = (Calendar) startDateTime.clone();
        endDateTime.add(Calendar.HOUR_OF_DAY, 2);

        updateDateTimeFields();
        loadResources();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilResource = findViewById(R.id.tilResource);
        tilStartDate = findViewById(R.id.tilStartDate);
        tilStartTime = findViewById(R.id.tilStartTime);
        tilEndDate = findViewById(R.id.tilEndDate);
        tilEndTime = findViewById(R.id.tilEndTime);
        tilPeople = findViewById(R.id.tilPeople);
        actvResource = findViewById(R.id.actvResource);
        etStartDate = findViewById(R.id.etStartDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndDate = findViewById(R.id.etEndDate);
        etEndTime = findViewById(R.id.etEndTime);
        etPeople = findViewById(R.id.etPeople);
        etNotes = findViewById(R.id.etNotes);
        btnSelectResource = findViewById(R.id.btnSelectResource);
        btnCreateReservation = findViewById(R.id.btnCreateReservation);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDateTimePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etStartTime.setOnClickListener(v -> showTimePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
        etEndTime.setOnClickListener(v -> showTimePicker(false));
    }

    private void setupButtons() {
        btnSelectResource.setOnClickListener(v -> loadAvailableResourcesForSelectedTime());
        btnCreateReservation.setOnClickListener(v -> validateAndSubmit());
    }

    private void loadResources() {
        showLoading(true);

        reservationRepository.getAllResources(new ReservationRepository.ResourceListCallback() {
            @Override
            public void onSuccess(List<ReservableResource> resources) {
                runOnUiThread(() -> {
                    showLoading(false);
                    availableResources = resources;
                    setupResourceAdapter();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreateReservationActivity.this,
                            "Error loading resources: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void loadAvailableResourcesForSelectedTime() {
        // Validate time selection first
        if (!endDateTime.after(startDateTime)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (startDateTime.getTimeInMillis() < System.currentTimeMillis()) {
            Toast.makeText(this, "Start time must be in the future", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        // Format date-times for API
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startTime = apiFormat.format(startDateTime.getTime());
        String endTime = apiFormat.format(endDateTime.getTime());

        reservationRepository.getAvailableResources(startTime, endTime, new ReservationRepository.ResourceListCallback() {
            @Override
            public void onSuccess(List<ReservableResource> resources) {
                runOnUiThread(() -> {
                    showLoading(false);
                    availableResources = resources;
                    setupResourceAdapter();
                    showAvailableResourcesDialog(resources);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreateReservationActivity.this,
                            "Error loading available resources: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void setupResourceAdapter() {
        List<String> resourceNames = new ArrayList<>();
        for (ReservableResource resource : availableResources) {
            resourceNames.add(resource.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                resourceNames
        );
        actvResource.setAdapter(adapter);

        actvResource.setOnItemClickListener((parent, view, position, id) -> {
            selectedResource = availableResources.get(position);
        });
    }

    private void showAvailableResourcesDialog(List<ReservableResource> resources) {
        if (resources.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("No Available Resources")
                    .setMessage("No resources are available for the selected time slot.\n\nTry selecting a different time.")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        String[] resourceNames = new String[resources.size()];
        for (int i = 0; i < resources.size(); i++) {
            ReservableResource resource = resources.get(i);
            resourceNames[i] = resource.getResourceIcon() + " " + resource.getName() + 
                    "\n   📍 " + (resource.getLocation() != null ? resource.getLocation() : "No location");
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Available Resources (" + resources.size() + ")")
                .setItems(resourceNames, (dialog, which) -> {
                    selectedResource = resources.get(which);
                    actvResource.setText(selectedResource.getName());
                    showResourceInfo();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showResourceSelectionDialog() {
        if (availableResources.isEmpty()) {
            Toast.makeText(this, "No resources available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] resourceNames = new String[availableResources.size()];
        for (int i = 0; i < availableResources.size(); i++) {
            resourceNames[i] = availableResources.get(i).getResourceIcon() + " " +
                    availableResources.get(i).getName();
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Resource")
                .setItems(resourceNames, (dialog, which) -> {
                    selectedResource = availableResources.get(which);
                    actvResource.setText(selectedResource.getName());
                    showResourceInfo();
                })
                .show();
    }

    private void showResourceInfo() {
        if (selectedResource == null) return;

        StringBuilder info = new StringBuilder();
        info.append("📍 Location: ").append(selectedResource.getDisplayLocation()).append("\n");
        if (selectedResource.getCapacity() != null) {
            info.append("👥 Capacity: ").append(selectedResource.getCapacity()).append("\n");
        }
        if (selectedResource.getRequiresKey()) {
            info.append("🔑 Key required at: ").append(selectedResource.getKeyLocation()).append("\n");
        }
        if (selectedResource.getMinReservationDuration() != null) {
            info.append("⏱️ Min duration: ").append(selectedResource.getMinReservationDuration()).append(" minutes\n");
        }
        if (selectedResource.getMaxReservationDuration() != null) {
            info.append("⏱️ Max duration: ").append(selectedResource.getMaxReservationDuration()).append(" minutes\n");
        }

        Toast.makeText(this, info.toString(), Toast.LENGTH_LONG).show();
    }

    private void showDatePicker(boolean isStart) {
        Calendar calendar = isStart ? startDateTime : endDateTime;

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeFields();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        picker.getDatePicker().setMinDate(System.currentTimeMillis());
        picker.show();
    }

    private void showTimePicker(boolean isStart) {
        Calendar calendar = isStart ? startDateTime : endDateTime;

        TimePickerDialog picker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDateTimeFields();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        picker.show();
    }

    private void updateDateTimeFields() {
        etStartDate.setText(displayDateFormat.format(startDateTime.getTime()));
        etStartTime.setText(timeFormat.format(startDateTime.getTime()));
        etEndDate.setText(displayDateFormat.format(endDateTime.getTime()));
        etEndTime.setText(timeFormat.format(endDateTime.getTime()));
    }

    private void validateAndSubmit() {
        if (selectedResource == null) {
            Toast.makeText(this, "Please select a resource", Toast.LENGTH_SHORT).show();
            return;
        }

        String peopleStr = etPeople.getText().toString().trim();
        int numberOfPeople = peopleStr.isEmpty() ? 1 : Integer.parseInt(peopleStr);
        String notes = etNotes.getText().toString().trim();

        // Format date-times for API
        SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String startTime = apiFormat.format(startDateTime.getTime());
        String endTime = apiFormat.format(endDateTime.getTime());

        CreateReservationRequest request = new CreateReservationRequest();
        request.setResourceId(selectedResource.getId());
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setNumberOfPeople(numberOfPeople);
        request.setNotes(notes.isEmpty() ? null : notes);

        String validationError = request.getValidationError();
        if (validationError != null) {
            Toast.makeText(this, validationError, Toast.LENGTH_LONG).show();
            return;
        }

        // Check time order
        if (!endDateTime.after(startDateTime)) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Client-side duration validation
        long durationMinutes = (endDateTime.getTimeInMillis() - startDateTime.getTimeInMillis()) / (1000 * 60);
        
        if (selectedResource.getMinReservationDuration() != null && 
            durationMinutes < selectedResource.getMinReservationDuration()) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Duration Too Short")
                    .setMessage("The minimum reservation duration for this resource is " + 
                            selectedResource.getMinReservationDuration() + " minutes.\n\n" +
                            "Your reservation is " + durationMinutes + " minutes.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        
        if (selectedResource.getMaxReservationDuration() != null && 
            durationMinutes > selectedResource.getMaxReservationDuration()) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle("Duration Too Long")
                    .setMessage("The maximum reservation duration for this resource is " + 
                            selectedResource.getMaxReservationDuration() + " minutes.\n\n" +
                            "Your reservation is " + durationMinutes + " minutes.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        submitReservation(request);
    }

    private void submitReservation(CreateReservationRequest request) {
        showLoading(true);

        reservationRepository.createReservation(request, new ReservationRepository.CreateReservationCallback() {
            @Override
            public void onSuccess(String message, Reservation reservation) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showSuccessDialog(reservation);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showErrorDialog(error);
                });
            }
        });
    }

    private void showErrorDialog(String error) {
        String userFriendlyMessage = parseErrorMessage(error);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cannot Create Reservation ❌")
                .setMessage(userFriendlyMessage)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", null)
                .show();
    }

    private String parseErrorMessage(String error) {
        if (error == null || error.isEmpty()) {
            return "An unknown error occurred. Please try again.";
        }

        String lowerError = error.toLowerCase();

        // Check for specific error messages - ORDER MATTERS!
        // Duration errors must be checked BEFORE limit errors because both may contain "maximum"
        if (lowerError.contains("too long") || lowerError.contains("too short") || 
            (lowerError.contains("duration") && !lowerError.contains("limit"))) {
            // Extract the duration limit if present
            if (error.contains("Maximum:")) {
                return "⚠️ Reservation Too Long\n\n" + error;
            } else if (error.contains("Minimum:")) {
                return "⚠️ Reservation Too Short\n\n" + error;
            }
            return "⚠️ Invalid Duration\n\nThe reservation duration does not meet the requirements for this resource.";
        }

        if (lowerError.contains("conflict") || lowerError.contains("already reserved")) {
            return "⚠️ Time Conflict\n\nThis resource is already reserved for the selected time slot. Please choose a different time.";
        }

        if (lowerError.contains("not available") || lowerError.contains("inactive")) {
            return "⚠️ Resource Unavailable\n\nThis resource is currently not available for reservations.";
        }

        if (lowerError.contains("daily") && lowerError.contains("limit")) {
            return "⚠️ Daily Limit Reached\n\nYou have reached the maximum number of reservations for this resource today.";
        }

        if (lowerError.contains("limit") || (lowerError.contains("maximum") && lowerError.contains("reservation"))) {
            return "⚠️ Reservation Limit Reached\n\nYou have reached the maximum number of reservations for this resource.";
        }

        if (lowerError.contains("capacity") || lowerError.contains("exceed")) {
            return "⚠️ Capacity Exceeded\n\nThe number of people exceeds the resource capacity.";
        }

        if (lowerError.contains("advance") || lowerError.contains("too far")) {
            return "⚠️ Booking Too Far in Advance\n\nYou cannot book more than 14 days in advance.";
        }

        if (lowerError.contains("past") || lowerError.contains("time")) {
            return "⚠️ Invalid Time\n\nThe selected time is in the past or too close to the current time.";
        }

        if (error.toLowerCase().contains("approval") || error.toLowerCase().contains("pending")) {
            return "⚠️ Approval Required\n\nThis resource requires admin approval. Your request will be reviewed.";
        }

        // If no specific match, return formatted error
        return "❌ Error\n\n" + error + "\n\nPlease check your reservation details and try again.";
    }

    private void showSuccessDialog(Reservation reservation) {
        StringBuilder message = new StringBuilder();
        message.append("Your reservation has been created successfully!\n\n");
        message.append("📍 Resource: ").append(reservation.getResourceName()).append("\n");
        message.append("📅 Time: ").append(reservation.getStartTime()).append("\n");

        if (Boolean.TRUE.equals(reservation.getRequiresKey())) {
            message.append("\n🔑 Don't forget to pick up the key at: ")
                    .append(reservation.getKeyLocation());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Reservation Created! 🎉")
                .setMessage(message.toString())
                .setIcon(R.drawable.ic_issues)
                .setPositiveButton("View Details", (dialog, which) -> {
                    Intent intent = new Intent(CreateReservationActivity.this, ReservationDetailsActivity.class);
                    intent.putExtra("reservation_id", reservation.getId());
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Done", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCreateReservation.setEnabled(!show);
    }
}