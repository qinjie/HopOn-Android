package com.example.sonata.hop_on.ParkingStation;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.sonata.hop_on.BicycleBooking.BookingActivity;
import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.LoanHistory.LoanDetailedActivity;
import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingStationListActivity extends AppCompatActivity {

    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<ParkingStationClass> data;
    static View.OnClickListener myOnClickListener;

    private static LatLng lastLocation;

    private static ArrayList<ParkingStationClass> nearestParkingStations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_station_list);

        recyclerView = (RecyclerView) findViewById(R.id.card_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        lastLocation = GlobalVariable.userLocation;
        getNearestParkingStation();

        initPlaceAutoCompleteSearch();
    }

    private void initPlaceAutoCompleteSearch()
    {
        try
        {
            PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                    getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    lastLocation = place.getLatLng();
                    getNearestParkingStation();
                }

                @Override
                public void onError(Status status) {
                    Log.i("OnPlaceSelected", "An error occurred: " + status);
                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void renewNearestParkingStationData(int size)
    {
        nearestParkingStations = new ArrayList<>(size);
    }

    private void getNearestParkingStation()
    {
        Preferences.showLoading(ParkingStationListActivity.this, "Parking Station", "Getting nearest parking station...");

        JsonObject loc = new JsonObject();
        try
        {
            loc.addProperty("latitude", lastLocation.latitude);
            loc.addProperty("longitude", lastLocation.longitude);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
            String auCode = pref.getString("authorizationCode", null);

            StringClient client = ServiceGenerator.createService(StringClient.class, auCode);
            Call<ResponseBody> call = client.getNearestParkingStations(loc);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        Preferences.dismissLoading();

                        JSONArray data =  new JSONArray(URLDecoder.decode( response.body().string(), "UTF-8" ));
                        int messageCode = response.code();

                        if (messageCode == 200) // SUCCESS
                        {
                            renewNearestParkingStationData(data.length());
                            for(int i = 0 ; i < data.length(); i++) {
                                JSONObject station = (JSONObject) data.get(i);

                                String latitude = station.getString("latitude");
                                String longitude = station.getString("longitude");
                                LatLng loc = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
                                String distance = station.getString("distanceText");

                                ParkingStationClass stationInfo = new ParkingStationClass(
                                        station.getString("id"),
                                        loc,
                                        station.getString("name"),
                                        station.getString("address"),
                                        station.getString("bicycle_count"),
                                        station.getString("available_bicycle"),
                                        distance
                                );

                                nearestParkingStations.add(stationInfo);
                            }

                            CustomParkingStationAdapter adapter = new CustomParkingStationAdapter(nearestParkingStations);
                            recyclerView.setAdapter(adapter);

                            addOnClickListenerToCardView();
                        }
                        else
                        {
                            if (messageCode == 500) // SERVER FAILED
                            {
                                Notification.showMessage(ParkingStationListActivity.this, 1);
                            }
                            else {

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void addOnClickListenerToCardView()
    {
        myOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int itemPosition = recyclerView.getChildLayoutPosition(view);
                GlobalVariable.setSelectedParkingStation(nearestParkingStations.get(itemPosition));
                Intent intent = new Intent(ParkingStationListActivity.this, BookingActivity.class);
                startActivity(intent);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_view, menu);

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
            case R.id.action_view_map:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
