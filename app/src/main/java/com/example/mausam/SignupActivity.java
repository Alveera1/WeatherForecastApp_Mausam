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

public class SignupActivity extends AppCompatActivity {

    private EditText etSignupEmail;
    private EditText etSignupPassword;
    private TextView tvSignupError;
    private ProgressBar progressBarSignup;
    private View btnSignup;
    private TextView tvGoToLogin;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect views from the layout to this code
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        tvSignupError = findViewById(R.id.tvSignupError);
        progressBarSignup = findViewById(R.id.progressBarSignup);
        btnSignup = findViewById(R.id.btnSignup);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        firebaseManager = new FirebaseManager();

        btnSignup.setOnClickListener(v -> attemptSignup());
        tvGoToLogin.setOnClickListener(v -> finish()); // just go back to Login
    }

    private void attemptSignup() {
        String email = etSignupEmail.getText().toString().trim();
        String password = etSignupPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.login_error_empty));
            return;
        }

        showLoading();
        firebaseManager.signUp(email, password,
                authResult -> {
                    hideLoading();
                    goToHome();
                },
                e -> {
                    hideLoading();
                    showError(e.getMessage() != null ? e.getMessage() : "Sign up failed. Try again.");
                });
    }

    private void goToHome() {
        startActivity(new Intent(SignupActivity.this, MainActivity.class));
        finish(); // so the back button doesn't return to Signup or Login
    }

    private void showLoading() {
        progressBarSignup.setVisibility(View.VISIBLE);
        tvSignupError.setVisibility(View.GONE);
        btnSignup.setEnabled(false);
    }

    private void hideLoading() {
        progressBarSignup.setVisibility(View.GONE);
        btnSignup.setEnabled(true);
    }

    private void showError(String message) {
        progressBarSignup.setVisibility(View.GONE);
        tvSignupError.setText(message);
        tvSignupError.setVisibility(View.VISIBLE);
        btnSignup.setEnabled(true);
    }
}