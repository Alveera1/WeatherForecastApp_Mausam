package com.example.mausam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvLoginError;
    private ProgressBar progressBarLogin;
    private View btnLogin;
    private TextView tvGoToSignup;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect views from the layout to this code
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        tvLoginError = findViewById(R.id.tvLoginError);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToSignup = findViewById(R.id.tvGoToSignup);

        firebaseManager = new FirebaseManager();

        // If already logged in from before, skip straight to Home
        if (firebaseManager.isUserLoggedIn()) {
            goToHome();
            return;
        }

        btnLogin.setOnClickListener(v -> attemptLogin());
        tvGoToSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.login_error_empty));
            return;
        }

        showLoading();
        firebaseManager.signIn(email, password,
                authResult -> {
                    hideLoading();
                    goToHome();
                },
                e -> {
                    hideLoading();
                    showError(e.getMessage() != null ? e.getMessage() : "Login failed. Try again.");
                });
    }

    private void goToHome() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish(); // so the back button doesn't return to Login
    }

    private void showLoading() {
        progressBarLogin.setVisibility(View.VISIBLE);
        tvLoginError.setVisibility(View.GONE);
        btnLogin.setEnabled(false);
    }

    private void hideLoading() {
        progressBarLogin.setVisibility(View.GONE);
        btnLogin.setEnabled(true);
    }

    private void showError(String message) {
        progressBarLogin.setVisibility(View.GONE);
        tvLoginError.setText(message);
        tvLoginError.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(true);
    }
}