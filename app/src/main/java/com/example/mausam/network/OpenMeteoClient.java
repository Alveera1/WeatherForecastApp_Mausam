package com.example.mausam.network;

import com.example.mausam.models.Forecast;
import com.example.mausam.models.MainWeather;
import com.example.mausam.models.WeatherCondition;
import com.example.mausam.models.Wind;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Forecast for one moment up to 16 days ahead, using Open-Meteo (free, no API key).
 * Self-contained: the Retrofit interface and response models are nested in this file.
 *
 * Blocking: call from a background thread (WeatherRepository already does).
 * The result is a Forecast in OpenWeatherMap units (°C, wind in m/s, condition words
 * like "Rain"), so PlanWeatherCheck and PlanChecker work unchanged.
 */
public class OpenMeteoClient {

    /** Thrown with a message that is safe to show to the user. */
    public static class ForecastException extends Exception {
        public ForecastException(String message) {
            super(message);
        }
    }

    public static final int FORECAST_DAYS = 16;
    private static final String GEOCODE_URL = "https://geocoding-api.open-meteo.com/v1/search";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast";
    private static final long MAX_GAP_MS = 90L * 60 * 1000; // hourly data -> nearest hour is within 30 min

    private static final Map<String, double[]> coordinateCache = new ConcurrentHashMap<>();

    private final Api api = RetrofitClient.getInstance().create(Api.class);

    public Forecast getForecastAt(String city, long dateTimeMillis) throws ForecastException {
        if (city == null || city.trim().isEmpty()) throw new ForecastException("City is empty");

        double[] coords = geocode(city.trim());

        ForecastBody body;
        try {
            Response<ForecastBody> res = api.hourlyForecast(
                    FORECAST_URL, coords[0], coords[1],
                    "temperature_2m,wind_speed_10m,weather_code",
                    FORECAST_DAYS, "GMT", "unixtime", "ms").execute();
            if (!res.isSuccessful() || res.body() == null) {
                throw new ForecastException(res.code() == 429
                        ? "Too many requests. Try again later."
                        : "Forecast service error (" + res.code() + ")");
            }
            body = res.body();
        } catch (IOException e) {
            throw new ForecastException("Unable to fetch weather. Check your internet connection.");
        }

        Hourly h = body.hourly;
        if (h == null || h.time == null || h.temperature == null
                || h.windSpeed == null || h.weatherCode == null) {
            throw new ForecastException("No forecast data available");
        }

        int i = closestIndex(h.time, dateTimeMillis);
        if (i < 0 || i >= h.temperature.size() || i >= h.windSpeed.size() || i >= h.weatherCode.size()
                || h.temperature.get(i) == null || h.windSpeed.get(i) == null
                || h.weatherCode.get(i) == null) {
            throw new ForecastException("No forecast for that time (up to "
                    + FORECAST_DAYS + " days ahead)");
        }

        return toForecast(h.time.get(i), h.temperature.get(i), h.windSpeed.get(i), h.weatherCode.get(i));
    }

    private double[] geocode(String city) throws ForecastException {
        String key = city.toLowerCase(Locale.ROOT);
        double[] cached = coordinateCache.get(key);
        if (cached != null) return cached;

        try {
            Response<GeocodeBody> res = api.geocode(GEOCODE_URL, city, 1).execute();
            if (!res.isSuccessful() || res.body() == null) {
                throw new ForecastException("Could not look up the city (" + res.code() + ")");
            }
            List<GeocodeResult> results = res.body().results;
            if (results == null || results.isEmpty()) throw new ForecastException("City not found");

            double[] coords = {results.get(0).latitude, results.get(0).longitude};
            coordinateCache.put(key, coords);
            return coords;
        } catch (IOException e) {
            throw new ForecastException("Unable to fetch weather. Check your internet connection.");
        }
    }

    // ---- Pure helpers (no Android/network), easy to unit-test ----

    /** Index of the hourly slot (unix seconds) closest to the target, or -1 if none within range. */
    public static int closestIndex(List<Long> timesSeconds, long targetMillis) {
        int best = -1;
        long bestDiff = Long.MAX_VALUE;
        for (int i = 0; i < timesSeconds.size(); i++) {
            Long t = timesSeconds.get(i);
            if (t == null) continue;
            long diff = Math.abs(t * 1000L - targetMillis);
            if (diff < bestDiff) {
                bestDiff = diff;
                best = i;
            }
        }
        return bestDiff <= MAX_GAP_MS ? best : -1;
    }

    /** WMO weather code -> OpenWeatherMap-style condition word. */
    public static String conditionForCode(int code) {
        if (code == 0) return "Clear";
        if (code >= 1 && code <= 3) return "Clouds";
        if (code == 45 || code == 48) return "Mist";
        if (code >= 51 && code <= 57) return "Drizzle";
        if ((code >= 61 && code <= 67) || (code >= 80 && code <= 82)) return "Rain";
        if ((code >= 71 && code <= 77) || code == 85 || code == 86) return "Snow";
        if (code >= 95 && code <= 99) return "Thunderstorm";
        return "Clouds";
    }

    static Forecast toForecast(long timeSeconds, double temp, double windMs, int code) {
        Forecast f = new Forecast();
        f.dt = timeSeconds;
        f.main = new MainWeather();
        f.main.temp = temp;
        f.wind = new Wind();
        f.wind.speed = windMs;
        WeatherCondition c = new WeatherCondition();
        c.main = conditionForCode(code);
        c.description = c.main.toLowerCase(Locale.ROOT);
        f.weather = new ArrayList<>();
        f.weather.add(c);
        return f;
    }

    // ---- Retrofit interface and response models (nested so this is the only file needed) ----

    interface Api {
        @GET
        Call<GeocodeBody> geocode(
                @Url String url,
                @Query("name") String city,
                @Query("count") int count);

        @GET
        Call<ForecastBody> hourlyForecast(
                @Url String url,
                @Query("latitude") double latitude,
                @Query("longitude") double longitude,
                @Query("hourly") String hourlyVariables,
                @Query("forecast_days") int days,
                @Query("timezone") String timezone,
                @Query("timeformat") String timeFormat,
                @Query("wind_speed_unit") String windUnit);
    }

    static class GeocodeBody {
        List<GeocodeResult> results; // null when the city isn't found
    }

    static class GeocodeResult {
        double latitude;
        double longitude;
    }

    static class ForecastBody {
        Hourly hourly;
    }

    static class Hourly {
        List<Long> time;
        @SerializedName("temperature_2m")
        List<Double> temperature;
        @SerializedName("wind_speed_10m")
        List<Double> windSpeed;
        @SerializedName("weather_code")
        List<Integer> weatherCode;
    }
}