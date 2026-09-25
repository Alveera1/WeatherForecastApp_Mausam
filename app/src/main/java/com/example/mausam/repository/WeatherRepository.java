package com.example.mausam.repository;

import com.example.mausam.models.Forecast;
import com.example.mausam.models.ForecastResponse;
import com.example.mausam.models.WeatherResponse;
import com.example.mausam.network.RetrofitClient;
import com.example.mausam.network.WeatherApi;
import com.example.mausam.utils.Constants;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {

    public interface ResultCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    private final WeatherApi api = RetrofitClient.getInstance().create(WeatherApi.class);

    public void getWeather(String city, ResultCallback<WeatherResponse> cb) {
        api.getCurrentWeather(city, Constants.API_KEY, Constants.UNITS)
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> res) {
                        if (res.isSuccessful() && res.body() != null) cb.onSuccess(res.body());
                        else cb.onError(messageForCode(res.code()));
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        cb.onError(messageForFailure(t));
                    }
                });
    }

    public void getForecast(String city, ResultCallback<List<Forecast>> cb) {
        api.getForecast(city, Constants.API_KEY, Constants.UNITS)
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> res) {
                        if (res.isSuccessful() && res.body() != null && res.body().list != null) {
                            List<Forecast> daily = new ArrayList<>();
                            List<Forecast> all = res.body().list;
                            for (int i = 0; i < all.size() && daily.size() < 5; i += 8) {
                                daily.add(all.get(i));
                            }
                            cb.onSuccess(daily);
                        } else {
                            cb.onError(messageForCode(res.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<ForecastResponse> call, Throwable t) {
                        cb.onError(messageForFailure(t));
                    }
                });
    }

    private String messageForCode(int code) {
        switch (code) {
            case 404: return "City not found";
            case 401: return "Invalid API key";
            case 429: return "Too many requests. Try again in a minute.";
            default:  return "Something went wrong (error " + code + ")";
        }
    }

    private String messageForFailure(Throwable t) {
        if (t instanceof SocketTimeoutException) return "Request timed out. Try again.";
        if (t instanceof IOException) return "Unable to fetch weather. Check your internet connection.";
        return "Unexpected error: " + t.getMessage();
    }
}