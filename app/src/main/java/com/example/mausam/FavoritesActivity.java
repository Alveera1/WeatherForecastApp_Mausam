package com.example.mausam;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView rvFavorites;
    private TextView tvEmptyFavorites;
    private FavoritesAdapter adapter;

    // The list currently shown on screen (fake data for now)
    private final List<FavoritesAdapter.FavoriteUiItem> currentItems = new ArrayList<>();

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

        loadPlaceholderFavorites();
    }

    // Fake data for designing the screen.
    // TODO (integration): replace with Alveera's FirebaseManager.getFavorites(listener)
    private void loadPlaceholderFavorites() {
        currentItems.clear();
        currentItems.add(new FavoritesAdapter.FavoriteUiItem("1", "Pune", "Partly Cloudy", "28°C"));
        currentItems.add(new FavoritesAdapter.FavoriteUiItem("2", "Mumbai", "Humid", "31°C"));
        currentItems.add(new FavoritesAdapter.FavoriteUiItem("3", "Delhi", "Sunny", "35°C"));
        showFavorites(currentItems);
    }

    // Runs when the user taps the trash icon on a row
    // TODO (integration): call FirebaseManager.deleteFavorite(item.id, onSuccess, onFailure)
    private void onDeleteClick(FavoritesAdapter.FavoriteUiItem item) {
        currentItems.remove(item);
        showFavorites(currentItems);
    }

    // Shows the list, or the "no favorites" message when the list is empty
    private void showFavorites(List<FavoritesAdapter.FavoriteUiItem> items) {
        adapter.setItems(new ArrayList<>(items));
        boolean isEmpty = items.isEmpty();
        tvEmptyFavorites.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvFavorites.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}