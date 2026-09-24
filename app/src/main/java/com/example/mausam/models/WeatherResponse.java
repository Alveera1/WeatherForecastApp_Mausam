package com.example.mausam.models;

import java.util.List;

public class WeatherResponse {
    public String name;
    public MainWeather main;
    public List<WeatherCondition> weather;
    public Wind wind;
}