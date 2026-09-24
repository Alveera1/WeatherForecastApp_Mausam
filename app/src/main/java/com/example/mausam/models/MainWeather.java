package com.example.mausam.models;

import com.google.gson.annotations.SerializedName;

public class MainWeather {
    public double temp;
    @SerializedName("feels_like")
    public double feelsLike;
    public int humidity;
    public int pressure;
}