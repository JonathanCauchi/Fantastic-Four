package com.example.neil.googlemapsdemo;

/**
 * Created by Neil on 3/6/2018.
 */

import com.example.neil.googlemapsdemo.mLatLng;
//error coming from ^^

public class RequestTicket {
    String status;
    String receivingLocationFrom;
    mLatLng latLng;
    int timeRemaining;

    public RequestTicket(){}

    public RequestTicket(String status, String receivingLocationFrom){
        this.status=status;
        this.receivingLocationFrom=receivingLocationFrom;
    }

    public RequestTicket(String status, String receivingLocation, mLatLng latLng, int timeRemaining){
        this.status = status;
        this.receivingLocationFrom = receivingLocation;
        this.latLng = latLng;
        this.timeRemaining= timeRemaining;
    }
}
