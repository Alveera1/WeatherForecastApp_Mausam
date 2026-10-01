package com.example.mausam;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mausam.models.Forecast;
import com.example.mausam.models.WeatherResponse;
import com.example.mausam.repository.WeatherRepository;
import com.example.mausam.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.ImageView;
import android.widget.ScrollView;

public class MainActivity extends AppCompatActivity {

    private View btnViewFavorites;
    private View btnViewPlans;
    private View btnLogout;
    private ImageButton btnFavorite;
    private EditText etSearchCity;
    private ProgressBar progressBar;
    private TextView tvError;
    private TextView tvCityName;
    private TextView tvTemperature;
    private TextView tvCondition;
    private TextView tvFeelsLike;
    private TextView tvHumidity;
    private TextView tvWind;
    private TextView tvPressure;
    private RecyclerView rvForecast;
    private ScrollView mainLayout;
    private ImageView ivWeatherIcon;
    private ForecastAdapter forecastAdapter;
    private FirebaseManager firebaseManager;
    private WeatherRepository weatherRepository;

    // Whether the current city is saved, and its Firebase document id (needed to delete it)
    private boolean isFavorite = false;
    private String favoriteDocId = null;

    // Real values from Person 2's API, filled in after a successful search
    private double currentTemperature = 0;
    private String currentCondition = "";

    // Android 13+ requires asking the user before we can send notifications
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> { /* Either way, the app continues working normally */ });

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
        btnViewPlans = findViewById(R.id.btnViewPlans);
        btnLogout = findViewById(R.id.btnLogout);
        btnFavorite = findViewById(R.id.btnFavorite);
        etSearchCity = findViewById(R.id.etSearchCity);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
        tvCityName = findViewById(R.id.tvCityName);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvCondition = findViewById(R.id.tvCondition);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvPressure = findViewById(R.id.tvPressure);
        rvForecast = findViewById(R.id.rvForecast);
        mainLayout = findViewById(R.id.main);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        // Alveera's Firebase code and Person 2's weather code
        firebaseManager = new FirebaseManager();
        weatherRepository = new WeatherRepository();

        // Initialize Notification channel and background alert worker (Person 3)
        NotificationHelper.createNotificationChannel(this);
        WeatherAlertWorker.schedulePeriodicWork(this);
        askNotificationPermission();

        // Set up the forecast row (scrolls sideways)
        forecastAdapter = new ForecastAdapter();
        rvForecast.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvForecast.setAdapter(forecastAdapter);

        // Search bar: runs when the user presses the search key on the keyboard
        etSearchCity.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchCity(etSearchCity.getText().toString().trim());
                return true;
            }
            return false;
        });

        // Heart button: save or remove the current city
        updateFavoriteIcon();
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // When tapped, open the Favorites screen
        btnViewFavorites.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class)));

        // When tapped, open the Plans screen
        btnViewPlans.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PlansActivity.class)));

        // When tapped, sign out and return to Login
        btnLogout.setOnClickListener(v -> {
            firebaseManager.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish(); // so the back button doesn't return to Home after logout
        });

        // Show a real city right away, so the screen isn't empty at first
        searchCity("Pune");
    }

    // Android 13+ requires asking the user before we can send notifications
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    // Asks Person 2's code for real weather, then shows it on the card
    private void searchCity(String city) {
        hideKeyboard();

        if (city.isEmpty()) {
            showError("Please enter a city name");
            return;
        }

        showLoading();

        weatherRepository.getWeather(city, new WeatherRepository.ResultCallback<WeatherResponse>() {
            @Override
            public void onSuccess(WeatherResponse data) {
                hideLoading();
                showWeather(data);
                firebaseManager.addSearchHistory(city);
                loadForecast(city);
            }

            @Override
            public void onError(String message) {
                hideLoading();
                showError(message);
            }
        });
    }

    // Fills the weather card with real data
    private void showWeather(WeatherResponse data) {
        tvCityName.setText(data.name);
        currentTemperature = data.main.temp;
        tvTemperature.setText(String.format(Locale.getDefault(), "%.0f°C", data.main.temp));
        tvFeelsLike.setText(String.format(Locale.getDefault(), "Feels like %.0f°C", data.main.feelsLike));
        tvHumidity.setText(String.format(Locale.getDefault(), "Humidity: %d%%", data.main.humidity));
        tvPressure.setText(String.format(Locale.getDefault(), "Pressure: %d hPa", data.main.pressure));

        if (data.wind != null) {
            tvWind.setText(String.format(Locale.getDefault(), "Wind: %.0f km/h", data.wind.speed));
        }

        if (data.weather != null && !data.weather.isEmpty()) {
            currentCondition = data.weather.get(0).main;
            tvCondition.setText(data.weather.get(0).description);

            applyWeatherTheme(currentCondition);
        }

        // A new city always starts as not-favorite; the Favorites screen shows the real status
        isFavorite = false;
        favoriteDocId = null;
        updateFavoriteIcon();
    }

    // Asks Person 2's code for the 5-day forecast, then fills the forecast row
    private void loadForecast(String city) {
        weatherRepository.getForecast(city, new WeatherRepository.ResultCallback<List<Forecast>>() {
            @Override
            public void onSuccess(List<Forecast> data) {
                showForecast(data);
            }

            @Override
            public void onError(String message) {
                // The main card already loaded, so just leave the forecast row as it was
            }
        });
    }

    private void showForecast(List<Forecast> forecastList) {
        List<ForecastAdapter.ForecastUiItem> uiItems = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        for (Forecast day : forecastList) {
            String dayName = dayFormat.format(new Date(day.dt * 1000L));
            String temp = String.format(Locale.getDefault(), "%.0f°C", day.main.temp);
            String icon = "⛅";
            if (day.weather != null && !day.weather.isEmpty()) {
                icon = emojiForCondition(day.weather.get(0).main);
            }
            uiItems.add(new ForecastAdapter.ForecastUiItem(dayName, icon, temp));
        }
        forecastAdapter.setItems(uiItems);
    }

    // Turns OpenWeatherMap's condition word into a simple emoji
    private String emojiForCondition(String condition) {
        switch (condition) {
            case "Clear": return "☀️";
            case "Clouds": return "⛅";
            case "Rain": return "🌧️";
            case "Drizzle": return "🌦️";
            case "Thunderstorm": return "⛈️";
            case "Snow": return "❄️";
            default: return "🌫️";
        }
    }
    private void applyWeatherTheme(String condition) {

        String cond = condition.toLowerCase(Locale.getDefault());

        int hour = java.util.Calendar.getInstance()
                .get(java.util.Calendar.HOUR_OF_DAY);

        boolean isNight = hour < 6 || hour >= 19;

        if (cond.contains("clear")) {

            if (isNight) {
                mainLayout.setBackgroundResource(R.drawable.bg_clear_night);
                ivWeatherIcon.setImageResource(R.drawable.ic_moon);
            } else {
                mainLayout.setBackgroundResource(R.drawable.bg_clear_day);
                ivWeatherIcon.setImageResource(R.drawable.ic_sun);
            }

        } else if (cond.contains("rain") || cond.contains("drizzle")) {

            mainLayout.setBackgroundResource(R.drawable.bg_rainy);
            ivWeatherIcon.setImageResource(R.drawable.ic_rain);

        } else if (cond.contains("cloud")) {

            mainLayout.setBackgroundResource(R.drawable.bg_cloudy);
            ivWeatherIcon.setImageResource(R.drawable.ic_cloud);

        } else {

            mainLayout.setBackgroundResource(R.drawable.bg_clear_day);
            ivWeatherIcon.setImageResource(R.drawable.ic_sun);
        }
    }

    // Saves the current city if it isn't saved, removes it if it is
    private void toggleFavorite() {
        btnFavorite.setEnabled(false); // stops double taps while Firebase is working

        if (!isFavorite) {
            FavoriteCity city = new FavoriteCity(
                    tvCityName.getText().toString(), currentTemperature, currentCondition);
            firebaseManager.addFavorite(
                    city,
                    documentReference -> {
                        isFavorite = true;
                        favoriteDocId = documentReference.getId();
                        updateFavoriteIcon();
                        btnFavorite.setEnabled(true);
                    },
                    e -> {
                        btnFavorite.setEnabled(true);
                        Toast.makeText(this, "Couldn't save city. Try again.",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            firebaseManager.deleteFavorite(
                    favoriteDocId,
                    unused -> {
                        isFavorite = false;
                        favoriteDocId = null;
                        updateFavoriteIcon();
                        btnFavorite.setEnabled(true);
                    },
                    e -> {
                        btnFavorite.setEnabled(true);
                        Toast.makeText(this, "Couldn't remove city. Try again.",
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // ---- Loading and error helpers ----

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
}