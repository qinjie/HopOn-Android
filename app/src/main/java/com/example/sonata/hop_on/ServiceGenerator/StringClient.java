package com.example.sonata.hop_on.ServiceGenerator;

import com.example.sonata.hop_on.LogIn.LogInClass;
import com.example.sonata.hop_on.SignUp.SignUpClass;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Tung on 5/31/2016.
 */
public interface StringClient {

    @GET("api/home")
    Call<String> getString();

    @GET("user/logout")
    Call<ResponseBody> logout();

    @GET("user/profile")
    Call<ResponseBody> getUserProfile();

    @GET("station/detail?stationId=1")
    Call<ResponseBody> getDetailStationInfo();

    @GET("rental/current-booking")
    Call<ResponseBody> getCurrentBookingInfo();

    @POST("user/login")
    Call<ResponseBody> login(@Body LogInClass up);

    @POST("user/signup")
    Call<ResponseBody> signup(@Body SignUpClass user);

    @POST("user/reset-password")
    Call<ResponseBody> resetPassword(@Body JsonObject email);

    @POST("user/change-password")
    Call<ResponseBody> changePassword(@Body JsonObject toUp);

    @POST("station/search")
    Call<ResponseBody> getNearestParkingStations(@Body JsonObject loc);

    @POST("bicycle/book")
    Call<ResponseBody> bicycleBooking();

    @POST("bicycle/return")
    Call<ResponseBody> bicycleReturning();

    @POST("rental/history")
    Call<ResponseBody> getBookingHistory();


}
