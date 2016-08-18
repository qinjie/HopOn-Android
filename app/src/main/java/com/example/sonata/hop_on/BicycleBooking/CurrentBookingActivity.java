package com.example.sonata.hop_on.BicycleBooking;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sonata.hop_on.BeaconService.BeaconScanningService;
import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentBookingActivity extends AppCompatActivity {

    private BookingInformationClass bookingInfo;
    private Button btn_return, btn_unlock;
    private Activity activity = this;

    private BroadcastReceiver mBeaconServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String type = intent.getStringExtra("type");
            if(type.equalsIgnoreCase("Return")) {
                Toast.makeText(getBaseContext(), "OK to Return", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(getBaseContext(), "OK to Unlock", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_booking);

        btn_return = (Button) findViewById(R.id.btn_return);
//        btn_return.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                GlobalVariable.setBookingStatusInSP(CurrentBookingActivity.this, GlobalVariable.FREE);
//
//            }
//        });

        btn_unlock = (Button) findViewById(R.id.button_unlock);

        this.setTitle("Current Booking");

        getCurrentBookingInformation();

        //TODO Start mornitoring beacon
        //TODO When EnterRegion, start ranging
        //TODO When beacon_station and beacon_bicycyle in range, enable return button
        //TODO When beacon_bicycle in range, enable unlock/lock button


    }

    private void getCurrentBookingInformation()
    {
        Preferences.showLoading(this, "Booking", "Sending request to server...");

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);

        Call<ResponseBody> call = client.getCurrentBookingInformation();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Preferences.dismissLoading();
                    int messageCode = response.code();

                    if (messageCode == 200)
                    {
                        JSONObject data = new JSONObject(response.body().string());
                        displayBookingInformation(data);
                        btn_return.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                registerReceiver(mBeaconServiceReceiver, new IntentFilter("OK_to_Return_or_Unlock"));
                                Intent intent = new Intent(activity, BeaconScanningService.class);
                                ArrayList<String> arrayList = new ArrayList<>();
                                arrayList.add(bookingInfo.beacon_bicycle_uuid);
                                arrayList.add(bookingInfo.beacon_bicycle_major);
                                arrayList.add(bookingInfo.beacon_bicycle_minor);
                                arrayList.add(bookingInfo.beacon_station_uuid);
                                arrayList.add(bookingInfo.beacon_station_major);
                                arrayList.add(bookingInfo.beacon_station_minor);
                                intent.putStringArrayListExtra("arrayList", arrayList);
                                activity.startService(intent);

                            }
                        });
                        btn_unlock.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                registerReceiver(mBeaconServiceReceiver, new IntentFilter("OK_to_Return_or_Unlock"));
                                Intent intent = new Intent(activity, BeaconScanningService.class);
                                ArrayList<String> arrayList = new ArrayList<>();
                                arrayList.add(bookingInfo.beacon_bicycle_uuid);
                                arrayList.add(bookingInfo.beacon_bicycle_major);
                                arrayList.add(bookingInfo.beacon_bicycle_minor);
                                intent.putStringArrayListExtra("arrayList", arrayList);
                                activity.startService(intent);
                            }
                        });
                    }
                    else if (messageCode == 400)
                    {

                    } else if (messageCode == 500)
                    {
                        Notification.showMessage(CurrentBookingActivity.this, 1);
                    }


                }
                catch(Exception e){

                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void displayBookingInformation(JSONObject data)
    {
        try
        {
            bookingInfo = new BookingInformationClass(data);

            TextView bookingIdText = (TextView) findViewById(R.id.booking_id_detail);
            bookingIdText.setText(bookingInfo.getBooking_id());

            TextView bicycleBrandDetailText = (TextView) findViewById(R.id.bicycle_brand_detail);
            bicycleBrandDetailText.setText(bookingInfo.getBicycleInfo());

            TextView pickUpStationText = (TextView) findViewById(R.id.pick_up_station_address);
            pickUpStationText.setText(bookingInfo.getPickUpStationAddress());

            TextView bookedTimeText = (TextView) findViewById(R.id.booked_time_detail);
            bookedTimeText.setText(bookingInfo.getBookedTime());

            TextView pickUpTimeText = (TextView) findViewById(R.id.pick_up_time_detail);
            pickUpTimeText.setText(bookingInfo.getPickUpTime());

            if (GlobalVariable.bookingMessage == true)
            {
                Toast.makeText(CurrentBookingActivity.this, "Booking successfully!", Toast.LENGTH_LONG);
                GlobalVariable.bookingMessage = false;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
