package com.example.mausam;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.example.mausam.models.Plan;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPlanActivity extends AppCompatActivity {

    private EditText etActivity;
    private EditText etLocation;
    private View btnPickDateTime;
    private TextView tvSelectedDateTime;
    private TextView tvAddPlanError;
    private ProgressBar progressBarAddPlan;
    private View btnSavePlan;
    private FirebaseManager firebaseManager;

    private final Calendar selectedDateTime = Calendar.getInstance();
    private boolean isDateTimePicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_plan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect views from the layout to this code
        etActivity = findViewById(R.id.etActivity);
        etLocation = findViewById(R.id.etLocation);
        btnPickDateTime = findViewById(R.id.btnPickDateTime);
        tvSelectedDateTime = findViewById(R.id.tvSelectedDateTime);
        tvAddPlanError = findViewById(R.id.tvAddPlanError);
        progressBarAddPlan = findViewById(R.id.progressBarAddPlan);
        btnSavePlan = findViewById(R.id.btnSavePlan);

        firebaseManager = new FirebaseManager();

        btnPickDateTime.setOnClickListener(v -> showDatePicker());
        btnSavePlan.setOnClickListener(v -> attemptSavePlan());
    }

    // Shows a date picker, then a time picker right after
    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(Calendar.YEAR, year);
            selectedDateTime.set(Calendar.MONTH, month);
            selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            showTimePicker();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            selectedDateTime.set(Calendar.MINUTE, minute);
            selectedDateTime.set(Calendar.SECOND, 0);
            isDateTimePicked = true;

            SimpleDateFormat format =
                    new SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault());
            tvSelectedDateTime.setText(format.format(selectedDateTime.getTime()));
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false)
                .show();
    }

    private void attemptSavePlan() {
        String activity = etActivity.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (activity.isEmpty() || location.isEmpty()) {
            showError("Please fill in both activity and location");
            return;
        }
        if (!isDateTimePicked) {
            showError("Please pick a date and time");
            return;
        }

        showLoading();

        String uid = firebaseManager.getCurrentUserId();
        Plan plan = new Plan(uid, activity, location, selectedDateTime.getTimeInMillis());

        new Thread(() -> {
            PlanWeatherCheck checker = new PlanWeatherCheck();
            PlanChecker.Result result = checker.check(activity, location, selectedDateTime.getTimeInMillis());
            android.util.Log.d("PlanDebug", "Result: " + (result == null ? "NULL" : result.toStatus()));
            plan.setStatus(result != null ? result.toStatus() : "pending");

            runOnUiThread(() -> firebaseManager.addPlan(plan,
                    docRef -> {
                        hideLoading();
                        finish();
                    },
                    e -> {
                        hideLoading();
                        showError(e.getMessage() != null ? e.getMessage() : "Couldn't save plan. Try again.");
                    }));
        }).start();
    }

    private void showLoading() {
        progressBarAddPlan.setVisibility(View.VISIBLE);
        tvAddPlanError.setVisibility(View.GONE);
        btnSavePlan.setEnabled(false);
    }

    private void hideLoading() {
        progressBarAddPlan.setVisibility(View.GONE);
        btnSavePlan.setEnabled(true);
    }

    private void showError(String message) {
        progressBarAddPlan.setVisibility(View.GONE);
        tvAddPlanError.setText(message);
        tvAddPlanError.setVisibility(View.VISIBLE);
        btnSavePlan.setEnabled(true);
    }
}