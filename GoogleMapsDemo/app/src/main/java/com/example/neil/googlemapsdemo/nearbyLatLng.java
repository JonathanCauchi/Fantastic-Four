package com.example.neil.googlemapsdemo;

/**
 * Created by Neil on 3/18/2018.
 */

public class nearbyLatLng {
    String userId;
    double latitude;
    double longitude;

    public nearbyLatLng(){}

    public nearbyLatLng(String userId, double latitude, double longitude){
        this.userId = userId;
        this.latitude=latitude;
        this.longitude=longitude;
    }

}
