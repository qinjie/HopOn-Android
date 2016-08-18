package com.example.sonata.hop_on.ParkingStation;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Sonata on 8/3/2016.
 */
public class ParkingStationClass {
    String stationId;
    LatLng location;
    String stationName;
    String stationAddress;
    String totalNumberOfBicycle;
    String numberOfAvailableBicycle;
    String distance;

    public ParkingStationClass(String _stationId, LatLng loc, String name, String address, String total, String avaiNumber, String _distance)
    {
        stationId = _stationId;
        location = loc;
        stationName = name;
        stationAddress = address;
        totalNumberOfBicycle = total;
        numberOfAvailableBicycle = avaiNumber;
        distance = _distance;
    }

    public LatLng getLocation()
    {
        return location;
    }

    public String getStationId() { return stationId; }

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
        return distance;
    }

    public String getNumberOfAvailableBicycle() { return numberOfAvailableBicycle; }

    public String getBikeInformation() { return String.valueOf(numberOfAvailableBicycle) + "/" + String.valueOf(totalNumberOfBicycle); }
}
