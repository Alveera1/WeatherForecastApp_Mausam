package com.example.mausam;

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

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavoritesActivity extends AppCompatActivity {

    private static final String TAG = "FavoritesActivity";
    private static final String EMPTY_MESSAGE =
            "No favorite cities yet.\nTap the heart on a city to save it.";

    private RecyclerView rvFavorites;
    private TextView tvEmptyFavorites;
    private FavoritesAdapter adapter;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect views from the layout to this code
        rvFavorites = findViewById(R.id.rvFavorites);
        tvEmptyFavorites = findViewById(R.id.tvEmptyFavorites);

        // Set up the list; the trash button calls onDeleteClick below
        adapter = new FavoritesAdapter(this::onDeleteClick);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(adapter);

        // Start listening to Alveera's Firebase code
        firebaseManager = new FirebaseManager();
        listenToFavorites();
    }

    // Live list: this runs once at the start, and again every time favorites change
    private void listenToFavorites() {
        firebaseManager.getFavorites((snapshots, error) -> {
            // Ignore updates that arrive after this screen was closed
            if (isFinishing() || isDestroyed()) return;

            if (error != null) {
                Log.e(TAG, "getFavorites failed", error);
                showMessage("Couldn't load favorites.\n" + error.getMessage());
                return;
            }

            List<FavoritesAdapter.FavoriteUiItem> items = new ArrayList<>();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    // Read only the fields we need (ignores the timestamp on purpose)
                    String city = doc.getString("city");
                    if (city == null) continue;

                    String condition = doc.getString("condition");
                    Double temperature = doc.getDouble("temperature");

                    items.add(new FavoritesAdapter.FavoriteUiItem(
                            doc.getId(),
                            city,
                            condition != null ? condition : "",
                            temperature != null
                                    ? String.format(Locale.getDefault(), "%.0f°C", temperature)
                                    : "--"));
                }
            }

            adapter.setItems(items);
            if (items.isEmpty()) {
                showMessage(EMPTY_MESSAGE);
            } else {
                showList();
            }
        });
    }

    // Runs when the user taps the trash icon on a row
    private void onDeleteClick(FavoritesAdapter.FavoriteUiItem item) {
        firebaseManager.deleteFavorite(
                item.id,
                unused -> { /* nothing to do: the live list refreshes by itself */ },
                e -> Toast.makeText(this, "Couldn't remove city. Try again.",
                        Toast.LENGTH_SHORT).show());
    }

    private void showList() {
        rvFavorites.setVisibility(View.VISIBLE);
        tvEmptyFavorites.setVisibility(View.GONE);
    }

    private void showMessage(String message) {
        tvEmptyFavorites.setText(message);
        tvEmptyFavorites.setVisibility(View.VISIBLE);
        rvFavorites.setVisibility(View.GONE);
    }
}