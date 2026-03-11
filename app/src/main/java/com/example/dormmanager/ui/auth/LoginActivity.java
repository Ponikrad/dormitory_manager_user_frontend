package com.example.dormmanager.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.MainActivity;
import com.example.dormmanager.R;
import com.example.dormmanager.data.model.LoginResponse;
import com.example.dormmanager.data.repository.AuthRepository;
import com.example.dormmanager.ui.common.LoadingDialog;
import com.example.dormmanager.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private LoadingDialog loadingDialog;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initRepository();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        loadingDialog = new LoadingDialog(this);
    }

    private void initRepository() {
        authRepository = new AuthRepository(this);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> login());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Please enter username");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter password");
            etPassword.requestFocus();
            return;
        }

        if (username.length() < Constants.MIN_USERNAME_LENGTH) {
            etUsername.setError("Username must be at least " + Constants.MIN_USERNAME_LENGTH + " characters");
            etUsername.requestFocus();
            return;
        }

        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            etPassword.setError("Password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters");
            etPassword.requestFocus();
            return;
        }

        loadingDialog.show();
        btnLogin.setEnabled(false);

        authRepository.login(username, password, new AuthRepository.AuthCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse data) {
                loadingDialog.dismiss();
                btnLogin.setEnabled(true);

                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                loadingDialog.dismiss();
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
}