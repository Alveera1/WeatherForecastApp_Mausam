package com.example.mausam;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        FirebaseManager firebaseManager = new FirebaseManager();
        boolean isLoggedIn = firebaseManager.isUserLoggedIn();

        // Wait 2 seconds, then open Home if logged in, otherwise Login
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> destination = isLoggedIn ? MainActivity.class : LoginActivity.class;
            startActivity(new Intent(SplashActivity.this, destination));
            finish(); // so the back button doesn't return to the splash
        }, SPLASH_DELAY_MS);
    }
}