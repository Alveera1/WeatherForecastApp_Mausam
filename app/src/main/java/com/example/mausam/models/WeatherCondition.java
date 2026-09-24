package com.example.mausam.models;

public class WeatherCondition {
    public String main;
    public String description;
    public String icon;

    public String getIconUrl() {
        return "https://openweathermap.org/img/wn/" + icon + "@2x.png";
    }
}