package com.example.mausam;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

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
    private ImageButton btnFavorite;
    private EditText etSearchCity;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvCityName;
    private RecyclerView rvForecast;
    private ForecastAdapter forecastAdapter;

    // Remembers whether the current city is a favorite (UI only for now)
    private boolean isFavorite = false;

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
        btnFavorite = findViewById(R.id.btnFavorite);
        etSearchCity = findViewById(R.id.etSearchCity);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvCityName = findViewById(R.id.tvCityName);
        rvForecast = findViewById(R.id.rvForecast);

        // Set up the forecast row (scrolls sideways)
        forecastAdapter = new ForecastAdapter();
        rvForecast.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvForecast.setAdapter(forecastAdapter);
        showPlaceholderForecast();

        // Search bar: runs when the user presses the search key on the keyboard
        etSearchCity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchCity(etSearchCity.getText().toString().trim());
                return true;
            }
            return false;
        });

        // Heart button: tap to switch between empty and filled
        updateFavoriteIcon();
        btnFavorite.setOnClickListener(v -> {
            isFavorite = !isFavorite;
            updateFavoriteIcon();
            // TODO (integration): call FirebaseManager.addFavorite / deleteFavorite here
        });

        // When tapped, open the Favorites screen
        btnViewFavorites.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class)));
    }

    // PRETEND search, used only to test the spinner and error UI.
    // TODO (integration): replace the delayed block with Person 2's getCurrentWeather() call.
    private void searchCity(String city) {
        hideKeyboard();

        if (city.isEmpty()) {
            showError("Please enter a city name");
            return;
        }

        showLoading();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            hideLoading();
            if (city.equalsIgnoreCase("error")) {
                showError("City not found");   // test word to see the error message
            } else {
                tvCityName.setText(city);
                isFavorite = false;
                updateFavoriteIcon();
            }
        }, 1500);
    }

    // ---- Loading and error helpers (Person 2's code will call these) ----

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearchCity.getWindowToken(), 0);
    }

    // Shows the filled heart when favorite, the empty heart when not
    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(
                isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        btnFavorite.setContentDescription(
                isFavorite ? "Remove from favorites" : "Add to favorites");
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