package com.example.uber.Model;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Request implements Serializable {
    private String requestId;
    private String requesterId;
    private String driverId;
    private double latitude;
    private double longitude;
    public Request() {
    }


    public Request(String requestId, String requesterId, String driverId, double latitude, double longitude) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.driverId = driverId;
        this.latitude = latitude;
        this.longitude = longitude;
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


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
}
