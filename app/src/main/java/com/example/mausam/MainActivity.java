package com.example.mausam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private View btnViewFavorites;
    private RecyclerView rvForecast;
    private ForecastAdapter forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Connect views from the layout to this code
        btnViewFavorites = findViewById(R.id.btnViewFavorites);
        rvForecast = findViewById(R.id.rvForecast);

        // Set up the forecast row (scrolls sideways)
        forecastAdapter = new ForecastAdapter();
        rvForecast.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvForecast.setAdapter(forecastAdapter);
        showPlaceholderForecast();

        // When tapped, open the Favorites screen
        btnViewFavorites.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class)));
    }

    // Fake data for designing the screen. Person 2's real data replaces this later.
    private void showPlaceholderForecast() {
        forecastAdapter.setItems(Arrays.asList(
                new ForecastAdapter.ForecastUiItem("Mon", "⛅", "28°C"),
                new ForecastAdapter.ForecastUiItem("Tue", "☀️", "31°C"),
                new ForecastAdapter.ForecastUiItem("Wed", "🌧️", "26°C"),
                new ForecastAdapter.ForecastUiItem("Thu", "⛈️", "24°C"),
                new ForecastAdapter.ForecastUiItem("Fri", "☀️", "30°C")
        ));
    }
}