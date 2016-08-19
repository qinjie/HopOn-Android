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
    String return_at;

    String beacon_station_uuid;
    String beacon_station_major;
    String beacon_station_minor;

    String beacon_bicycle_uuid;
    String beacon_bicycle_major;
    String beacon_bicycle_minor;

    String return_station_name;
    String return_station_address;
    String return_station_postal;

    String return_station_lat;
    String return_station_lng;

    String duration;

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

    public BookingInformationClass(JSONObject data, boolean isLoanHistory)
    {
        try
        {
            if (isLoanHistory == true)
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
                return_at = data.getString("return_at");

                duration = data.getString("duration");

                return_station_name = data.getString("return_station_name");
                return_station_address = data.getString("return_station_address");
                return_station_postal = data.getString("return_station_postal");

                return_station_lat = data.getString("return_station_lat");
                return_station_lng = data.getString("return_station_lng");
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public String getBooking_id() { return booking_id; }
    public String getBicycleBrand() { return brand; }
    public String getBicycleInfo() { return brand + " / " + model; }

    public String getPickUpStationAddress() { return pickup_station_address; }
    public String getReturnStationAddress() { return return_station_address; }

    public String getBookedTime() { return book_at; }
    public String getPickUpTime() { return pickup_at; }
    public String getReturnTime() { return return_at; }

    public String getDuration() { return duration; }
}
