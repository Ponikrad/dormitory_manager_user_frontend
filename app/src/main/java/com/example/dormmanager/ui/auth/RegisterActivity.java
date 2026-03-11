package com.example.dormmanager.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.MainActivity;
import com.example.dormmanager.R;
import com.example.dormmanager.data.model.LoginResponse;
import com.example.dormmanager.data.model.RegisterRequest;
import com.example.dormmanager.data.repository.AuthRepository;
import com.example.dormmanager.ui.common.LoadingDialog;
import com.example.dormmanager.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private TextInputEditText etFirstName, etLastName, etPhoneNumber, etRoomNumber;
    private Button btnRegister;
    private TextView tvLogin;
    private LoadingDialog loadingDialog;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        initRepository();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etRoomNumber = findViewById(R.id.etRoomNumber);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        loadingDialog = new LoadingDialog(this);
    }

    private void initRepository() {
        authRepository = new AuthRepository(this);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> register());

        tvLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String roomNumber = etRoomNumber.getText().toString().trim();

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() ||
                confirmPassword.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < Constants.MIN_USERNAME_LENGTH) {
            etUsername.setError("Username must be at least " + Constants.MIN_USERNAME_LENGTH + " characters");
            etUsername.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            etPassword.setError("Password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Create request
        RegisterRequest request = new RegisterRequest(username, email, password, firstName, lastName);
        request.setPhoneNumber(phoneNumber.isEmpty() ? null : phoneNumber);
        request.setRoomNumber(roomNumber.isEmpty() ? null : roomNumber);

        // Show loading
        loadingDialog.show();
        btnRegister.setEnabled(false);

        // Call API
        authRepository.register(request, new AuthRepository.AuthCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse data) {
                loadingDialog.dismiss();
                btnRegister.setEnabled(true);

                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                // Navigate to MainActivity
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                loadingDialog.dismiss();
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}