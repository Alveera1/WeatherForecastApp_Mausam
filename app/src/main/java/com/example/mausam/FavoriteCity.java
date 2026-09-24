package com.example.mausam;
public class FavoriteCity {

    private String city;
    private double temperature;
    private String condition;
    private long timestamp;

    public FavoriteCity() {
        // Required empty constructor for Firestore
    }

    public FavoriteCity(String city, double temperature, String condition) {
        this.city = city;
        this.temperature = temperature;
        this.condition = condition;
        this.timestamp = System.currentTimeMillis();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}