package com.example.mausam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mausam.models.Plan;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.List;

public class PlansActivity extends AppCompatActivity {

    private static final String TAG = "PlansActivity";

    private RecyclerView rvPlans;
    private TextView tvEmptyPlans;
    private FloatingActionButton fabAddPlan;
    private PlansAdapter adapter;
    private FirebaseManager firebaseManager;
    private ListenerRegistration plansListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_plans);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect views from the layout to this code
        rvPlans = findViewById(R.id.rvPlans);
        tvEmptyPlans = findViewById(R.id.tvEmptyPlans);
        fabAddPlan = findViewById(R.id.fabAddPlan);

        // Set up the list; the trash button calls onDeleteClick below
        adapter = new PlansAdapter(this::onDeleteClick);
        rvPlans.setLayoutManager(new LinearLayoutManager(this));
        rvPlans.setAdapter(adapter);

        firebaseManager = new FirebaseManager();

        // Open the Add Plan form
        fabAddPlan.setOnClickListener(v ->
                startActivity(new Intent(PlansActivity.this, AddPlanActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start listening when the screen becomes visible
        plansListener = firebaseManager.getPlans((snapshots, error) -> {
            if (isFinishing() || isDestroyed()) return;

            if (error != null) {
                Log.e(TAG, "getPlans failed", error);
                showMessage("Couldn't load plans.\n" + error.getMessage());
                return;
            }

            List<Plan> plans = snapshots != null
                    ? snapshots.toObjects(Plan.class)
                    : List.of();

            adapter.setItems(plans);
            if (plans.isEmpty()) {
                showMessage(getString(R.string.empty_plans));
            } else {
                showList();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop listening when the screen is hidden, to save battery and data
        if (plansListener != null) {
            plansListener.remove();
        }
    }

    // Runs when the user taps the trash icon on a row
    private void onDeleteClick(Plan plan) {
        firebaseManager.deletePlan(
                plan.getId(),
                unused -> { /* nothing to do: the live list refreshes by itself */ },
                e -> Toast.makeText(this, "Couldn't delete plan. Try again.",
                        Toast.LENGTH_SHORT).show());
    }

    private void showList() {
        rvPlans.setVisibility(View.VISIBLE);
        tvEmptyPlans.setVisibility(View.GONE);
    }

    private void showMessage(String message) {
        tvEmptyPlans.setText(message);
        tvEmptyPlans.setVisibility(View.VISIBLE);
        rvPlans.setVisibility(View.GONE);
    }
}