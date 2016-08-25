package com.example.sonata.hop_on.ServiceGenerator;

import com.example.sonata.hop_on.LogIn.LogInClass;
import com.example.sonata.hop_on.SignUp.SignUpClass;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

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

    @GET("station/detail")
    Call<ResponseBody> getDetailStationInfo(@Query("stationId") String stationId);

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

    @POST("user/change-email")
    Call<ResponseBody> changeEmail(@Body JsonObject toUp);

    @POST("station/search")
    Call<ResponseBody> getNearestParkingStations(@Body JsonObject loc);

    @POST("bicycle/lock")
    Call<ResponseBody> bicycleLock(@Body JsonObject bicycleInfo);

    @POST("bicycle/unlock")
    Call<ResponseBody> bicycleUnlock(@Body JsonObject bicycleInfo);

    @POST("bicycle/book")
    Call<ResponseBody> bicycleBooking(@Body JsonObject bookingInfo);

    @POST("bicycle/return")
    Call<ResponseBody> bicycleReturning(@Body JsonObject returningInfo);

    @POST("rental/history")
    Call<ResponseBody> getBookingHistory();

    @POST("rental/current-booking")
    Call<ResponseBody> getCurrentBookingInformation();

    @POST("feedback/new")
    Call<ResponseBody> getFeedback(@Body JsonObject feedback);



}
