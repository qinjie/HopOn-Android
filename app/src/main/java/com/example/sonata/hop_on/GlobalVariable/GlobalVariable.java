package com.example.sonata.hop_on.GlobalVariable;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;


import com.example.sonata.hop_on.LogIn.LogInActivity;
import com.example.sonata.hop_on.LogIn.LogInClass;
import com.example.sonata.hop_on.ParkingStation.ParkingStationClass;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Vector;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Lord One on 6/7/2016.
 */
public class GlobalVariable {
    public static final String apiKey = "8253a8dfd06d885e754ef8c596d4e809";
    public static final String apiSecret = "HlTQpKjISJ0Fxp1kkd4COSf12-_ErMrH";

    public static String userName;

    public static Activity activity;

    public static int mNumberOfBicycleStation;

    public static ArrayList<ParkingStationClass> nearestParkingStations;

    public static LatLng currentLocation;

    public static void setCurrentLocation(LatLng location)
    {
        currentLocation = location;
    }

    public static void renewNearestParkingStationData()
    {
        nearestParkingStations.clear();
    }

    public static void addDataToNearestParkingStationList(ParkingStationClass station)
    {
        nearestParkingStations.add(station);
    }

    public static void checkConnected(final Activity activity) {
        String username = "123";
        String password = "321";
        StringClient client = ServiceGenerator.createService(StringClient.class);
        LogInClass up = new LogInClass(username, password);

        Call<ResponseBody> call = client.login(up);
        try {
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    int messageCode = response.code();
                    if(messageCode != 200 && messageCode != 400)
                    {

                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }

    public static void logoutAction(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("ATK_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);
        Call<ResponseBody> call = client.logout();

        Intent intent = new Intent(activity, LogInActivity.class);
        activity.startActivity(intent);
    }

    public static void setAuCodeInSP(Activity activity, String authorizationCode) {
        SharedPreferences pref = activity.getSharedPreferences("ATK_pref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("authorizationCode", "Bearer " + authorizationCode);
        editor.apply();
    }

    public static void setUserName(String _username)
    {
        userName = _username;
    }

    public static String getUserName()
    {
        return userName;
    }

    public static int getNumberOfBicycleStations() { return mNumberOfBicycleStation; }
}
