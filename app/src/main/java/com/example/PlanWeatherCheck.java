package com.example.mausam;

import com.example.mausam.models.Forecast;
import com.example.mausam.repository.WeatherRepository;

/**
 * Person 2's half of the worker: given a plan's activity, location and time,
 * fetch the forecast for that slot and run PlanChecker on it.
 * Person 3's WeatherAlertWorker calls this for each plan and fires the notification.
 * Blocking, so call it from a background thread (doWork()).
 */
public class PlanWeatherCheck {

    private final WeatherRepository repository = new WeatherRepository();

    /** Returns null if the forecast couldn't be fetched (leave the plan as "pending"). */
    public PlanChecker.Result check(String activity, String location, long dateTimeMillis) {
        Forecast slot = repository.getForecastAtSync(location, dateTimeMillis);
        if (slot == null || slot.main == null) return null;

        String condition = (slot.weather != null && !slot.weather.isEmpty())
                ? slot.weather.get(0).main : "";
        double windKmh = slot.wind != null ? slot.wind.speed * 3.6 : 0; // API gives m/s

        return PlanChecker.evaluateDetailed(activity, condition, windKmh, slot.main.temp);
    }
}