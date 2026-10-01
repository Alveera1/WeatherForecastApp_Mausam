package com.example.mausam.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Forecast {
    public long dt;
    @SerializedName("dt_txt")
    public String dtTxt;
    public MainWeather main;
    public Wind wind;
    public List<WeatherCondition> weather;
}