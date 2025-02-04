package com.example.sonata.hop_on.BicycleBooking;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sonata.hop_on.BeaconService.BeaconScanningService;
import com.example.sonata.hop_on.Feedback.FeedbackActivity;
import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.NavigationDrawer.NavigationDrawerActivity;
import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.punchthrough.bean.sdk.Bean;
import com.punchthrough.bean.sdk.BeanDiscoveryListener;
import com.punchthrough.bean.sdk.BeanListener;
import com.punchthrough.bean.sdk.BeanManager;
import com.punchthrough.bean.sdk.message.BeanError;
import com.punchthrough.bean.sdk.message.ScratchBank;

import org.altbeacon.beacon.Beacon;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrentBookingActivity extends AppCompatActivity
        implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    public static final String TAG = CurrentBookingActivity.class.getSimpleName();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleApiClient mGoogleApiClient;

    private BookingInformationClass bookingInfo;

    private Button btn_return, btn_unlock;
    private Activity activity = this;

    private static boolean isValidPickUpTime = false;

    Location mLastLocation = null;
    private LocationRequest mLocationRequest;

    private static BeanListener beanListener;

    private static Bean bean = null;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        Log.i(TAG, "Location services connected.");
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
        {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            else {
                handleNewLocation(mLastLocation);
            };
            return;
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        mLastLocation = location;
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void initBeans() {
        final List<Bean> beans = new ArrayList<>();

        BeanDiscoveryListener listener = new BeanDiscoveryListener() {
            @Override
            public void onBeanDiscovered(Bean iBean, int rssi) {
                beans.add(iBean);

                Toast.makeText(getBaseContext(), iBean.getDevice().getName(), Toast.LENGTH_SHORT).show();

                System.out.println(iBean.getDevice().getName());   // "Bean"              (example)
                System.out.println(iBean.getDevice().getAddress());    // "B4:99:4C:1E:BC:75" (example)

                try
                {
                    if (bookingInfo.getBeanAddress().compareTo(iBean.getDevice().getAddress()) == 0)
                    {
                        String temp = "Found " + iBean.getDevice().getName() + " bean!";
                        Toast.makeText(getBaseContext(), temp, Toast.LENGTH_SHORT).show();

                        bean = iBean;

                        bean.connect(CurrentBookingActivity.this, beanListener);
                        BeanManager.getInstance().cancelDiscovery();
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDiscoveryComplete() {
                // This is called when the scan times out, defined by the .setScanTimeout(int seconds) method

                for (Bean iBean : beans) {
                    System.out.println(iBean.getDevice().getName());   // "Bean"              (example)
                    System.out.println(iBean.getDevice().getAddress());    // "B4:99:4C:1E:BC:75" (example)
                }

                Toast.makeText(getBaseContext(), "Finish scanning beans!", Toast.LENGTH_SHORT).show();
                // Assume we have a reference to the 'beans' ArrayList from above.
                System.out.println("list beancons " + beans.size());
            }
        };

        BeanManager.getInstance().setScanTimeout(30);  // Timeout in seconds, optional, default is 30 seconds
        BeanManager.getInstance().startDiscovery(listener);
    }

    private void sendSerial(String command)
    {
        String data = bookingInfo.enc + "," + bookingInfo.auth_key + "," + command;

        System.out.println("command data: " + data);
        bean.sendSerialMessage(data);
        bean.endSerialGate();
    }

    private BroadcastReceiver mBeaconServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (GlobalVariable.beaconScanning == false)
            {
                return;
            }

            GlobalVariable.beaconScanning = false;
            Preferences.dismissLoading();
            String type = intent.getStringExtra("type");
            if(type.equalsIgnoreCase("Return")) {
                if (bean != null)
                {
                    sendSerial("3");
                }
                else
                {
                    Toast.makeText(getBaseContext(), "Can not scan bean", Toast.LENGTH_SHORT).show();
                }
            }else if(type.equalsIgnoreCase("Unlock")) {
                Toast.makeText(getBaseContext(), "Scan device successfully", Toast.LENGTH_SHORT).show();

                SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
                String bicycleStatus = pref.getString("bicycleStatus", null);

                if (bicycleStatus.compareTo(GlobalVariable.LOCKED) == 0) {
                    if (bean != null)
                    {
                        sendSerial("1");
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "Can not scan bean", Toast.LENGTH_SHORT).show();
                    }
                }
                else if (bicycleStatus.compareTo(GlobalVariable.UNLOCKED) == 0)
                {
                    if (bean != null)
                    {
                        sendSerial("2");
                    }
                    else
                    {
                        Toast.makeText(getBaseContext(), "Can not scan bean", Toast.LENGTH_SHORT).show();
                    }
                }

            } else{
                Toast.makeText(getBaseContext(), "Failed to scan beacon.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void requestUnlockBicycle()
    {
        Preferences.showLoading(this, "Unlocking bicycle", "Sending request to server...");

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);

        JsonObject unlockInfo = new JsonObject();
        try
        {
            unlockInfo.addProperty("bicycleId", bookingInfo.bicycle_id);

            if (mLastLocation == null)
            {
                Toast.makeText(getBaseContext(), "There is no current location", Toast.LENGTH_SHORT).show();
                return;
            }

            String latitude = String.valueOf(mLastLocation.getLatitude());
            unlockInfo.addProperty("latitude", latitude);

            String longitude = String.valueOf(mLastLocation.getLongitude());
            unlockInfo.addProperty("longitude", longitude);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        Call<ResponseBody> call = client.bicycleUnlock(unlockInfo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Preferences.dismissLoading();
                    int messageCode = response.code();

                    if (messageCode == 200)
                    {
                        Toast.makeText(getBaseContext(), "Unlock successfully!", Toast.LENGTH_LONG).show();

                        btn_unlock.setText("LOCK");
                        GlobalVariable.setBicycleStatusInSP(CurrentBookingActivity.this, GlobalVariable.UNLOCKED);

                        if (isValidPickUpTime == false)
                        {
                            getCurrentBookingInformation();
                        }

                        isValidPickUpTime = true;
                    }
                    else if (messageCode == 400)
                    {
                        Toast.makeText(getBaseContext(), "Invalid data!", Toast.LENGTH_LONG).show();
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

    private void requestLockBicycle()
    {
        Preferences.showLoading(this, "Locking bicycle", "Sending request to server...");

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);

        JsonObject lockInfo = new JsonObject();
        try
        {
            lockInfo.addProperty("bicycleId", bookingInfo.bicycle_id);

            if (mLastLocation == null)
            {
                Toast.makeText(getBaseContext(), "There is no current location", Toast.LENGTH_SHORT).show();
                return;
            }

            String latitude = String.valueOf(mLastLocation.getLatitude());
            lockInfo.addProperty("latitude", latitude);

            String longitude = String.valueOf(mLastLocation.getLongitude());
            lockInfo.addProperty("longitude", longitude);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        Call<ResponseBody> call = client.bicycleLock(lockInfo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Preferences.dismissLoading();
                    int messageCode = response.code();
                    if (messageCode == 200)
                    {
                        Toast.makeText(getBaseContext(), "Lock successfully!", Toast.LENGTH_LONG).show();
                        btn_unlock.setText("UNLOCK");
                        GlobalVariable.setBicycleStatusInSP(CurrentBookingActivity.this, GlobalVariable.LOCKED);
                    }
                    else if (messageCode == 400)
                    {
                        Toast.makeText(getBaseContext(), "Invalid data!", Toast.LENGTH_LONG).show();
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

    private void sendReturnRequestToServer()
    {
        Preferences.showLoading(this, "Returning", "Sending request to server...");

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);

        JsonObject returnInfo = new JsonObject();
        try
        {
            returnInfo.addProperty("bicycleId", bookingInfo.bicycle_id);

            if (mLastLocation == null)
            {
                Toast.makeText(getBaseContext(), "There is no current location", Toast.LENGTH_SHORT).show();
                return;
            }

            String latitude = String.valueOf(mLastLocation.getLatitude());
            returnInfo.addProperty("latitude", latitude);

            String longitude = String.valueOf(mLastLocation.getLongitude());
            returnInfo.addProperty("longitude", longitude);

            JsonArray beaconList = new JsonArray();

            for(int i = 0; i < GlobalVariable.beaconArrayList.size(); i++)
            {
                Beacon beacon = GlobalVariable.beaconArrayList.get(i);
                JsonObject beaconInfo = new JsonObject();

                beaconInfo.addProperty("uuid", beacon.getId1().toString().toUpperCase());
                beaconInfo.addProperty("major", beacon.getId2().toString());
                beaconInfo.addProperty("minor", beacon.getId3().toString());
                beaconInfo.addProperty("rssi", beacon.getRssi());

                beaconList.add(beaconInfo);
            }

            returnInfo.add("listBeacons", beaconList);
        } catch (Exception e)
        {
            e.printStackTrace();
            JsonArray beaconList = new JsonArray();
            returnInfo.add("listBeacons", beaconList);
        }

        Call<ResponseBody> call = client.bicycleReturning(returnInfo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Preferences.dismissLoading();
                    int messageCode = response.code();

                    if (messageCode == 200)
                    {
                        if (bean != null)
                        {
                            bean.disconnect();
                        }

                        isValidPickUpTime = false;

                        GlobalVariable.bookingMessage = false;
                        GlobalVariable.setBookingStatusInSP(CurrentBookingActivity.this, GlobalVariable.FREE);

                        GlobalVariable.selectedParkingStation = null;
                        GlobalVariable.selectedBicycle = null;

                        Toast.makeText(getBaseContext(), "Return successfully!", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(CurrentBookingActivity.this, FeedbackActivity.class);
                        startActivity(intent);
                    }
                    else if (messageCode == 400)
                    {
                        Toast.makeText(getBaseContext(), "Invalid data!", Toast.LENGTH_LONG).show();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_booking);

        this.setTitle("Current Booking");

        btn_return = (Button) findViewById(R.id.btn_return);
        btn_unlock = (Button) findViewById(R.id.button_unlock);

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String bicycleStatus = pref.getString("bicycleStatus", null);

        if (bicycleStatus == null)
        {
            GlobalVariable.setBicycleStatusInSP(CurrentBookingActivity.this, GlobalVariable.LOCKED);
        }

        bicycleStatus = pref.getString("bicycleStatus", null);
        if (bicycleStatus.compareTo(GlobalVariable.LOCKED) == 0)
        {
            btn_unlock.setText("UNLOCK");
        }
        else
        {
            btn_unlock.setText("LOCK");
        }

        getCurrentBookingInformation();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds

        beanListener = new BeanListener() {

            @Override
            public void onConnected() {
                Toast.makeText(getBaseContext(), "Connected to bean!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionFailed() {
                Toast.makeText(getBaseContext(), "Connected failed to bean!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnected() {
                Toast.makeText(getBaseContext(), "Disconnect to bean!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSerialMessageReceived(byte[] bytes) {
                try
                {
                        String str = new String(bytes, "US-ASCII");
                        int value = Integer.valueOf(str);
                        Toast.makeText(getBaseContext(), str, Toast.LENGTH_SHORT).show();

                        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
                        String bicycleStatus = pref.getString("bicycleStatus", null);

                    switch (value)
                    {
                        case 0:
                            Toast.makeText(getBaseContext(), "Bean received return command!", Toast.LENGTH_SHORT).show();
                            sendReturnRequestToServer();
                            break;
                        case 1:
                            if (bicycleStatus.compareTo(GlobalVariable.UNLOCKED) == 0)
                            {
                                Toast.makeText(getBaseContext(), "Bean received lock command!", Toast.LENGTH_SHORT).show();
                                requestLockBicycle();
                            }
                            break;
                        case 2:
                            if (bicycleStatus.compareTo(GlobalVariable.LOCKED) == 0)
                            {
                                Toast.makeText(getBaseContext(), "Bean received unlock command!", Toast.LENGTH_SHORT).show();
                                requestUnlockBicycle();
                            }
                            break;
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onScratchValueChanged(ScratchBank scratchBank, byte[] bytes) {

            }

            @Override
            public void onError(BeanError beanError) {
                String temp = "onError " + beanError;
                Toast.makeText(getBaseContext(), temp, Toast.LENGTH_SHORT).show();
                System.out.println("onError " + beanError);
            }

            @Override
            public void onReadRemoteRssi(int i) {

            }
        };
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();

        if (bean != null && bean.isConnected())
        {
            bean.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        if (bean != null && bean.isConnected())
        {
            bean.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

        if (bean != null)
        {
            bean.connect(CurrentBookingActivity.this, beanListener);
        }

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
                                if (!isValidPickUpTime)
                                {
                                    sendReturnRequestToServer();
                                    return;
                                }
                                else
                                {
                                    GlobalVariable.beaconScanning = true;
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
                                    Preferences.showLoading(activity, "Checking Beacon", "Scanning device...");
                                    activity.startService(intent);
                                }
                            }
                        });

                        btn_unlock.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                GlobalVariable.beaconScanning = true;
                                registerReceiver(mBeaconServiceReceiver, new IntentFilter("OK_to_Return_or_Unlock"));
                                Intent intent = new Intent(activity, BeaconScanningService.class);
                                ArrayList<String> arrayList = new ArrayList<>();
                                arrayList.add(bookingInfo.beacon_bicycle_uuid);
                                arrayList.add(bookingInfo.beacon_bicycle_major);
                                arrayList.add(bookingInfo.beacon_bicycle_minor);
                                intent.putStringArrayListExtra("arrayList", arrayList);

                                Preferences.showLoading(activity, "Bicycle", "Scanning device...");
                                activity.startService(intent);
                            }
                        });

                        initBeans();
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
            GlobalVariable.setCurrentBookingInfo(bookingInfo);

            TextView bookingIdText = (TextView) findViewById(R.id.booking_id_detail);
            bookingIdText.setText(bookingInfo.getBooking_id());

            TextView brandText = (TextView) findViewById(R.id.bicycle_brand);
            brandText.setText(bookingInfo.getBicycleInfo());

            TextView bicycleBrandDetailText = (TextView) findViewById(R.id.bicycle_brand_detail);
            bicycleBrandDetailText.setText(bookingInfo.getBicycleSerial());

            TextView pickUpStationNameText = (TextView) findViewById(R.id.pick_up_station_name);
            pickUpStationNameText.setText(bookingInfo.getPickUpStationName());

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
                Intent intent = new Intent(CurrentBookingActivity.this, NavigationDrawerActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}


