package com.example.sonata.hop_on.GlobalVariable;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.sonata.hop_on.BicycleBooking.BicycleInformationClass;
import com.example.sonata.hop_on.BicycleBooking.BookingInformationClass;
import com.example.sonata.hop_on.LogIn.LogInActivity;
import com.example.sonata.hop_on.LogIn.LogInClass;
import com.example.sonata.hop_on.ParkingStation.ParkingStationClass;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.example.sonata.hop_on.UserProfile.UserProfile;
import com.google.android.gms.maps.model.LatLng;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

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

    public static final String FREE   = "0";
    public static final String BOOKED = "1";

    public static final String LOCKED = "2";
    public static final String UNLOCKED = "3";

    public static UserProfile userProfile;

    public static Activity activity;

    public static LatLng userLocation;

    public static ParkingStationClass selectedParkingStation = null;

    public static BicycleInformationClass selectedBicycle = null;

    public static BookingInformationClass selectedLoanRecord = null;

    public static BookingInformationClass currentBookingInfo = null;

    public static ArrayList<Beacon> beaconArrayList = null;

    public static boolean bookingMessage = false;

    public static boolean beaconScanning = false;

    public static void setUserProfile(UserProfile _userProfile)
    {
        userProfile = _userProfile;
    }

    public static UserProfile getUserProfile()
    {
        return userProfile;
    }

    public static void setCurrentBookingInfo(BookingInformationClass bookingInfo)
    {
        currentBookingInfo = bookingInfo;
    }

    public static void setBeaconArrayList(ArrayList<Beacon> beacons)
    {
        beaconArrayList = new ArrayList<>(beacons.size());

        for(int i = 0; i < beacons.size(); i++)
        {
            beaconArrayList.add(beacons.get(i));
        }
    }

    public static void setSelectedLoanRecord(BookingInformationClass record)
    {
        selectedLoanRecord = record;
    }

    public static BookingInformationClass getSelectedLoanRecord()
    {
        return selectedLoanRecord;
    }

    public static void setSelectedParkingStation(ParkingStationClass station)
    {
        selectedParkingStation = station;
    }

    public static ParkingStationClass getSelectedParkingStation() { return selectedParkingStation; }

    public static void setSelectedBicycle(BicycleInformationClass bicycle)
    {
        selectedBicycle = bicycle;
    }

    public static BicycleInformationClass getSelectedBicycle()
    {
        return selectedBicycle;
    }

    public static void setCurrentLocation(LatLng location)
    {
        userLocation = location;
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

    public static boolean obtainedAuCode (Activity activity) {
        SharedPreferences pref = activity.getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);
        if (auCode != null && auCode != "{\"password\":[\"Incorrect username or password.\"]}"){
            return true;
        }
        return false;
    }

    public static void logoutAction(Activity activity){
        SharedPreferences pref = activity.getSharedPreferences("HopOn_pref", 0);
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
        SharedPreferences pref = activity.getSharedPreferences("HopOn_pref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("authorizationCode", "Bearer " + authorizationCode);
        editor.apply();
    }

    public static void setBookingStatusInSP(Activity activity, String status)
    {
        SharedPreferences pref = activity.getSharedPreferences("HopOn_pref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("bookingStatus", status);
        editor.apply();
    }

    public static void setBicycleStatusInSP(Activity activity, String status)
    {
        SharedPreferences pref = activity.getSharedPreferences("HopOn_pref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("bicycleStatus", status);
        editor.apply();
    }

    public static boolean isSelectedParkingStation(LatLng position)
    {
        if (selectedParkingStation.getLocation() == position)
            return true;

        return false;
    }

    public static boolean isAvailableBicycleAtSelectedStation()
    {
        if (selectedParkingStation == null)
            return false;

        int availableBicycle = Integer.valueOf(selectedParkingStation.getNumberOfAvailableBicycle());
        if (availableBicycle > 0)
            return true;

        return false;
    }
}
