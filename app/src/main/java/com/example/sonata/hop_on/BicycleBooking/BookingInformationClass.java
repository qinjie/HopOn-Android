package com.example.sonata.hop_on.BicycleBooking;

import org.json.JSONObject;

/**
 * Created by Sonata on 8/18/2016.
 */

public class BookingInformationClass {
    String booking_id;
    String bicycle_id;

    String bicycle_serial;
    String desc;
    String brand;
    String model;

    String pickup_station_name;
    String pickup_station_address;
    String pickup_station_postal;

    String pickup_station_lat;
    String pickup_station_lng;

    String book_at;
    String pickup_at;

    String beacon_station_uuid;
    String beacon_station_major;
    String beacon_station_minor;

    String beacon_bicycle_uuid;
    String beacon_bicycle_major;
    String beacon_bicycle_minor;

    public BookingInformationClass() {};
    public BookingInformationClass(JSONObject data)
    {
        try
        {
            booking_id = data.getString("booking_id");
            bicycle_id = data.getString("bicycle_id");

            bicycle_serial = data.getString("bicycle_serial");
            desc = data.getString("desc");
            brand = data.getString("brand");
            model = data.getString("model");

            pickup_station_name = data.getString("pickup_station_name");
            pickup_station_address = data.getString("pickup_station_address");
            pickup_station_postal = data.getString("pickup_station_postal");

            pickup_station_lat = data.getString("pickup_station_lat");
            pickup_station_lng = data.getString("pickup_station_lng");

            book_at = data.getString("book_at");
            pickup_at = data.getString("pickup_at");

            beacon_station_uuid = data.getString("beacon_station_uuid");
            beacon_station_major = data.getString("beacon_station_major");
            beacon_station_minor = data.getString("beacon_station_minor");

            beacon_bicycle_uuid = data.getString("beacon_bicycle_uuid");
            beacon_bicycle_major = data.getString("beacon_bicycle_major");
            beacon_bicycle_minor = data.getString("beacon_bicycle_minor");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    String getBooking_id() { return booking_id; }
    String getBicycleInfo() { return brand + " / " + model; }
    String getPickUpStationAddress() { return pickup_station_address; }
    String getBookedTime() { return book_at; }
    String getPickUpTime() { return pickup_at; }
}
