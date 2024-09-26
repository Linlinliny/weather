package com.example.weather;

public class Location {
    private double latitude;
    private double longitude;
    private String city;
    private String district;

    public Location(double latitude, double longitude, String city, String district) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.city = city;
        this.district = district;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }
}