package com.example.dormmanager.ui.payments;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.dormmanager.R;
import com.example.dormmanager.data.model.CreatePaymentRequest;
import com.example.dormmanager.data.repository.PaymentRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CreatePaymentActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputLayout tilPaymentType, tilAmount, tilDescription;
    private AutoCompleteTextView actvPaymentType;
    private TextInputEditText etAmount, etDescription;
    private MaterialCheckBox cbCard, cbBlik, cbBankTransfer;
    private MaterialCardView cardMethodCard, cardMethodBlik, cardMethodBank;
    private MaterialButton btnCreatePayment;
    private View loadingOverlay;

    private PaymentRepository paymentRepository;

    private static final String[] PAYMENT_TYPES = {
            "RENT", "UTILITIES", "DEPOSIT", "FINE", "OTHER"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_payment);

        initViews();
        setupToolbar();
        setupPaymentTypeDropdown();
        setupPaymentMethods();
        setupSubmitButton();

        paymentRepository = new PaymentRepository(this);
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tilPaymentType = findViewById(R.id.tilPaymentType);
        tilAmount = findViewById(R.id.tilAmount);
        tilDescription = findViewById(R.id.tilDescription);
        actvPaymentType = findViewById(R.id.actvPaymentType);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);

        cbCard = findViewById(R.id.cbCard);
        cbBlik = findViewById(R.id.cbBlik);
        cbBankTransfer = findViewById(R.id.cbBankTransfer);

        cardMethodCard = findViewById(R.id.cardMethodCard);
        cardMethodBlik = findViewById(R.id.cardMethodBlik);
        cardMethodBank = findViewById(R.id.cardMethodBank);

        btnCreatePayment = findViewById(R.id.btnCreatePayment);
        loadingOverlay = findViewById(R.id.loadingOverlay);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupPaymentTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                PAYMENT_TYPES
        );
        actvPaymentType.setAdapter(adapter);
    }

    private void setupPaymentMethods() {
        // Card - click on card OR checkbox
        cardMethodCard.setOnClickListener(v -> toggleCheckbox(cbCard));
        cbCard.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbBlik.setChecked(false);
                cbBankTransfer.setChecked(false);
                updateCardStrokes();
            }
        });

        // BLIK - click on card OR checkbox
        cardMethodBlik.setOnClickListener(v -> toggleCheckbox(cbBlik));
        cbBlik.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbCard.setChecked(false);
                cbBankTransfer.setChecked(false);
                updateCardStrokes();
            }
        });

        // Bank Transfer - click on card OR checkbox
        cardMethodBank.setOnClickListener(v -> toggleCheckbox(cbBankTransfer));
        cbBankTransfer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                cbCard.setChecked(false);
                cbBlik.setChecked(false);
                updateCardStrokes();
            }
        });

        // Set initial stroke for card (default selected)
        updateCardStrokes();
    }

    private void toggleCheckbox(MaterialCheckBox checkbox) {
        checkbox.setChecked(!checkbox.isChecked());
    }

    private void updateCardStrokes() {
        int primaryColor = ContextCompat.getColor(this, R.color.md_theme_light_primary);
        int outlineColor = ContextCompat.getColor(this, R.color.md_theme_light_outline);

        cardMethodCard.setStrokeColor(cbCard.isChecked() ? primaryColor : outlineColor);
        cardMethodCard.setStrokeWidth(cbCard.isChecked() ? 4 : 2);

        cardMethodBlik.setStrokeColor(cbBlik.isChecked() ? primaryColor : outlineColor);
        cardMethodBlik.setStrokeWidth(cbBlik.isChecked() ? 4 : 2);

        cardMethodBank.setStrokeColor(cbBankTransfer.isChecked() ? primaryColor : outlineColor);
        cardMethodBank.setStrokeWidth(cbBankTransfer.isChecked() ? 4 : 2);
    }

    private void setupSubmitButton() {
        btnCreatePayment.setOnClickListener(v -> validateAndSubmit());
    }

    private void validateAndSubmit() {
        // Get values
        String paymentType = actvPaymentType.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validate
        boolean isValid = true;

        if (paymentType.isEmpty()) {
            tilPaymentType.setError("Please select payment type");
            isValid = false;
        } else {
            tilPaymentType.setError(null);
        }

        if (amountStr.isEmpty()) {
            tilAmount.setError("Please enter amount");
            isValid = false;
        } else {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    tilAmount.setError("Amount must be greater than 0");
                    isValid = false;
                } else {
                    tilAmount.setError(null);
                }
            } catch (NumberFormatException e) {
                tilAmount.setError("Invalid amount");
                isValid = false;
            }
        }

        if (description.isEmpty()) {
            tilDescription.setError("Please enter description");
            isValid = false;
        } else if (description.length() < 5) {
            tilDescription.setError("Description too short (min 5 characters)");
            isValid = false;
        } else {
            tilDescription.setError(null);
        }

        // Check if at least one payment method is selected
        if (!cbCard.isChecked() && !cbBlik.isChecked() && !cbBankTransfer.isChecked()) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isValid) return;

        // Get payment method
        String paymentMethod = "CARD";
        if (cbBlik.isChecked()) {
            paymentMethod = "BLIK";
        } else if (cbBankTransfer.isChecked()) {
            paymentMethod = "BANK_TRANSFER";
        }

        // Create payment
        createPayment(paymentType, Double.parseDouble(amountStr), description, paymentMethod);
    }

    private void createPayment(String paymentType, double amount, String description, String paymentMethod) {
        showLoading(true);

        CreatePaymentRequest request = new CreatePaymentRequest(
                amount, paymentMethod, description, paymentType);

        paymentRepository.createPayment(request, new PaymentRepository.CreatePaymentCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showSuccessDialog();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CreatePaymentActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showSuccessDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Payment Created")
                .setMessage("Your payment has been created successfully!")
                .setIcon(R.drawable.ic_payment)
                .setPositiveButton("OK", (dialog, which) -> {
                    finish(); // Return to payments list
                })
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCreatePayment.setEnabled(!show);
    }
}