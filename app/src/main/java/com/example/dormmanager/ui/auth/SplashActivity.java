package com.example.dormmanager.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dormmanager.MainActivity;
import com.example.dormmanager.R;
import com.example.dormmanager.data.local.TokenManager;
import com.example.dormmanager.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        tokenManager = new TokenManager(this);

        // Delay for splash screen duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUserStatus();
        }, Constants.SPLASH_DURATION);
    }

    private void checkUserStatus() {
        if (tokenManager.isLoggedIn()) {
            // User is logged in, go to MainActivity
            navigateToMain();
        } else {
            // User is not logged in, go to LoginActivity
            navigateToLogin();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}