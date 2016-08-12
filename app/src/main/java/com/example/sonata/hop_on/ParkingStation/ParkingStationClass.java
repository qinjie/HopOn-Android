package com.example.sonata.hop_on.ParkingStation;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Sonata on 8/3/2016.
 */
public class ParkingStationClass {
    LatLng location;
    String stationName;
    String stationAddress;
    int totalNumberOfBicycle;
    int numberOfAvailableBicycle;
    String distance;

    public ParkingStationClass(LatLng loc, String name, String address, int total, int avaiNumber)
    {
        location = loc;
        stationName = name;
        stationAddress = address;
        totalNumberOfBicycle = total;
        numberOfAvailableBicycle = avaiNumber;
    }

    public LatLng getLocation()
    {
        return location;
    }

    public String getStationName()
    {
        return stationName;
    }

    public String getAvailableBicycles()
    {
        return String.valueOf(numberOfAvailableBicycle) + " bicycles";
    }

    public String getTotalBicycles()
    {
        return "Total " + String.valueOf(totalNumberOfBicycle) + " bicycles";
    }

    public String getStationAddress()
    {
        return stationAddress;
    }

    public String getDistance()
    {
        return distance + " m";
    }

    public int getNumberOfAvailableBicycle() { return numberOfAvailableBicycle; }
}
