package com.example.uber.Model;

public class Driver {
    private double latitude;
    private double longitude;
    private String driverId;
    public Driver() {
    }

    public Driver(double latitude, double longitude, String driverId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.driverId = driverId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
