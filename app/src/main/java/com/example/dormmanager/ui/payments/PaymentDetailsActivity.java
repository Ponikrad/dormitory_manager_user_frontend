package com.example.dormmanager.ui.payments;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.Payment;
import com.example.dormmanager.data.repository.PaymentRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;

public class PaymentDetailsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvStatusIcon, tvAmount, tvDescription, tvPaymentType;
    private TextView tvPaymentMethod, tvCreatedDate;
    private Chip chipStatus;
    private MaterialButton btnDownloadReceipt, btnProcessPayment;
    private FrameLayout progressBar; // ZMIENIONE z CircularProgressIndicator

    private PaymentRepository paymentRepository;
    private Payment currentPayment;
    private Long paymentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_details);

        initViews();
        setupToolbar();

        paymentRepository = new PaymentRepository(this);

        paymentId = getIntent().getLongExtra("payment_id", -1);
        if (paymentId == -1) {
            Toast.makeText(this, "Invalid payment", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPaymentDetails();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvStatusIcon = findViewById(R.id.tvStatusIcon);
        tvAmount = findViewById(R.id.tvAmount);
        tvDescription = findViewById(R.id.tvDescription);
        tvPaymentType = findViewById(R.id.tvPaymentType);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvCreatedDate = findViewById(R.id.tvCreatedDate);
        chipStatus = findViewById(R.id.chipStatus);
        btnDownloadReceipt = findViewById(R.id.btnDownloadReceipt);
        btnProcessPayment = findViewById(R.id.btnProcessPayment);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupButtons() {
        btnDownloadReceipt.setOnClickListener(v -> downloadReceipt());
        btnProcessPayment.setOnClickListener(v -> processPayment());
    }

    private void loadPaymentDetails() {
        showLoading(true);

        paymentRepository.getPaymentById(paymentId, new PaymentRepository.SinglePaymentCallback() {
            @Override
            public void onSuccess(Payment payment) {
                runOnUiThread(() -> {
                    showLoading(false);
                    currentPayment = payment;
                    displayPaymentDetails(payment);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(PaymentDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void displayPaymentDetails(Payment payment) {
        tvAmount.setText(payment.getFormattedAmount());
        tvDescription.setText(payment.getDescription() != null ?
                payment.getDescription() : "Payment");
        tvPaymentType.setText(payment.getPaymentType() != null ?
                payment.getPaymentType() : "GENERAL");

        String method = formatPaymentMethod(payment.getPaymentMethod());
        tvPaymentMethod.setText(method);

        String date = formatDate(payment.getCreatedAt());
        tvCreatedDate.setText(date);

        String status = payment.getStatusDisplay() != null ?
                payment.getStatusDisplay() : payment.getStatus();
        chipStatus.setText(status);

        String statusIcon = getStatusIcon(payment.getStatus());
        tvStatusIcon.setText(statusIcon);
        chipStatus.setChipBackgroundColorResource(getStatusColor(payment.getStatus()));

        if ("PENDING".equals(payment.getStatus())) {
            btnProcessPayment.setVisibility(View.VISIBLE);
        } else {
            btnProcessPayment.setVisibility(View.GONE);
        }

        btnDownloadReceipt.setEnabled("COMPLETED".equals(payment.getStatus()));
    }

    private String formatPaymentMethod(String method) {
        if (method == null) return "Unknown";

        switch (method.toUpperCase()) {
            case "CARD":
                return "Card Payment";
            case "BLIK":
                return "BLIK";
            case "BANK_TRANSFER":
                return "Bank Transfer";
            case "CASH":
                return "Cash";
            default:
                return method;
        }
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "Unknown date";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat(
                    "MMM dd, yyyy HH:mm", Locale.getDefault());

            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateString.substring(0, Math.min(10, dateString.length()));
        }
    }

    private String getStatusIcon(String status) {
        if (status == null) return "⏳";

        switch (status.toUpperCase()) {
            case "COMPLETED":
                return "✅";
            case "PENDING":
            case "PROCESSING":
                return "⏳";
            case "FAILED":
            case "CANCELLED":
                return "❌";
            default:
                return "💳";
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return R.color.status_pending;

        switch (status.toUpperCase()) {
            case "COMPLETED":
                return R.color.status_completed;
            case "PENDING":
            case "PROCESSING":
                return R.color.status_pending;
            case "FAILED":
            case "CANCELLED":
                return R.color.status_failed;
            default:
                return R.color.status_pending;
        }
    }

    private void processPayment() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Process Payment")
                .setMessage("Are you sure you want to process this payment?")
                .setPositiveButton("Process", (dialog, which) -> {
                    performProcessPayment();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performProcessPayment() {
        showLoading(true);

        paymentRepository.processPayment(paymentId, new PaymentRepository.ProcessPaymentCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(PaymentDetailsActivity.this,
                            "Payment processed successfully!", Toast.LENGTH_SHORT).show();
                    loadPaymentDetails();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(PaymentDetailsActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void downloadReceipt() {
        showLoading(true);

        paymentRepository.downloadReceipt(paymentId, new PaymentRepository.ReceiptCallback() {
            @Override
            public void onSuccess(ResponseBody body) {
                runOnUiThread(() -> {
                    showLoading(false);
                    saveAndOpenReceipt(body);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(PaymentDetailsActivity.this,
                            "Error downloading receipt: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveAndOpenReceipt(ResponseBody body) {
        try {
            String fileName = "payment_receipt_" + paymentId + ".pdf";
            Uri fileUri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (API 29+) - Use MediaStore
                fileUri = saveFileUsingMediaStore(fileName, body);
            } else {
                // Android 9 and below - Use traditional file system
                fileUri = saveFileTraditional(fileName, body);
            }

            if (fileUri != null) {
                Toast.makeText(this, "Receipt downloaded successfully",
                        Toast.LENGTH_LONG).show();
                openPdf(fileUri);
            } else {
                Toast.makeText(this, "Error saving receipt",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Error saving receipt: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private Uri saveFileUsingMediaStore(String fileName, ResponseBody body) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

            Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (InputStream inputStream = body.byteStream();
                     OutputStream outputStream = getContentResolver().openOutputStream(uri)) {

                    if (outputStream != null) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                        return uri;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Uri saveFileTraditional(String fileName, ResponseBody body) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);

            if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
                return null;
            }

            File receiptFile = new File(downloadsDir, fileName);

            try (InputStream inputStream = body.byteStream();
                 FileOutputStream outputStream = new FileOutputStream(receiptFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }

            // Return FileProvider URI for opening
            return FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", receiptFile);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openPdf(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            startActivity(intent);
        } catch (Exception e) {
            // Try alternative: open with chooser
            Intent chooser = Intent.createChooser(intent, "Open PDF with");
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(chooser);
            } catch (Exception e2) {
                Toast.makeText(this, "No PDF viewer found. File saved to Downloads.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}