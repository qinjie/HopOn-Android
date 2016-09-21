package com.example.sonata.hop_on.BicycleBooking;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.LogIn.LogInActivity;
import com.example.sonata.hop_on.NavigationDrawer.NavigationDrawerActivity;
import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.OnSwipeTouchListener;
import com.example.sonata.hop_on.ParkingStation.ParkingStationClass;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.text.Text;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.analytics.internal.zzy.e;

public class BookingActivity extends AppCompatActivity {

    ArrayList<BicycleInformationClass> selectedParkingStationInfo;
    int selectedBicycleIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        this.setTitle("Booking a Bicycle");
        setStationInfo();

        Preferences.showLoading(this, "Setup", "Loading data from server...");

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);
        String stationId = GlobalVariable.getSelectedParkingStation().getStationId();
        Call<ResponseBody> call = client.getDetailStationInfo(stationId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Preferences.dismissLoading();

                    JSONArray data = new JSONArray(URLDecoder.decode(response.body().string(), "UTF-8"));
                    int messageCode = response.code();

                    if (messageCode == 200) {
                        renewParkingStationInfo(data.length());
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject bicycle = (JSONObject) data.get(i);

                            String bicycleId = bicycle.getString("bicycle_type_id");
                            String bicycleBrand = bicycle.getString("brand");
                            String bicycleMode = bicycle.getString("model");
                            String availabelNumber = bicycle.getString("availableBicycle");
                            String totalNumber = bicycle.getString("totalBicycle");

                            JSONArray imageUrl = new JSONArray(bicycle.getString("listImageUrl"));
                            ArrayList<String> listImageUrl = new ArrayList<String>(imageUrl.length());
                            for(int j = 0; j < imageUrl.length(); j++)
                            {
                                String url = imageUrl.getString(j);
                                listImageUrl.add(url);
                            }

                            BicycleInformationClass bicycleInfo = new BicycleInformationClass(
                                    bicycleId,
                                    bicycleBrand,
                                    bicycleMode,
                                    availabelNumber,
                                    totalNumber,
                                    listImageUrl
                            );

                            selectedParkingStationInfo.add(bicycleInfo);
                        }

                        if (data.length() > 0) {
                            showSelectedBicycleInfo(selectedBicycleIndex);

                            TextView notification = (TextView) findViewById(R.id.notification);
                            if (data.length() == 1)
                            {
                                notification.setText("<- Only 1 model is available ->");
                            }
                            else
                            {
                                notification.setText("<- Change the model ->");
                            }
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

        try {
            Button btn_left = (Button) findViewById(R.id.button_left);
            btn_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedBicycleIndex--;

                    if (selectedBicycleIndex < 0)
                    {
                        selectedBicycleIndex = selectedParkingStationInfo.size() - 1;
                    }

                    showSelectedBicycleInfo(selectedBicycleIndex);
                }
            });

            Button btn_right = (Button) findViewById(R.id.button_right);
            btn_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedBicycleIndex++;

                    if (selectedBicycleIndex == selectedParkingStationInfo.size())
                    {
                        selectedBicycleIndex = 0;
                    }

                    showSelectedBicycleInfo(selectedBicycleIndex);
                }
            });

            Button btn_bookNow = (Button) findViewById(R.id.button_book);
            String bookingStatus = pref.getString("bookingStatus", null);
            if (bookingStatus.compareTo(GlobalVariable.FREE) == 0)
            {
                btn_bookNow.setEnabled(true);
                btn_bookNow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestToBookBicycle();
                    }
                });
            }
            else if (bookingStatus.compareTo(GlobalVariable.BOOKED) == 0)
            {
                btn_bookNow.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestToBookBicycle()
    {
        BicycleInformationClass selectedBicycle = selectedParkingStationInfo.get(selectedBicycleIndex);
        GlobalVariable.setSelectedBicycle(selectedBicycle);

        Preferences.showLoading(this, "Booking", "Sending request to server...");

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);

        JsonObject bookingInfo = new JsonObject();
        try
        {
            String stationId = GlobalVariable.getSelectedParkingStation().getStationId();
            bookingInfo.addProperty("stationId", stationId);

            String bicycleTypeId = GlobalVariable.getSelectedBicycle().getBicycleId();
            bookingInfo.addProperty("bicycleTypeId", bicycleTypeId);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        Call<ResponseBody> call = client.bicycleBooking(bookingInfo);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Preferences.dismissLoading();
                    int messageCode = response.code();

                    if (messageCode == 200)
                    {
                        GlobalVariable.bookingMessage = true;
                        GlobalVariable.setBookingStatusInSP(BookingActivity.this, GlobalVariable.BOOKED);
                        GlobalVariable.setBicycleStatusInSP(BookingActivity.this, GlobalVariable.LOCKED);

                        Intent intent = new Intent(BookingActivity.this, CurrentBookingActivity.class);
                        startActivity(intent);
                    }
                    else if (messageCode == 400)
                    {
                        Notification.showMessage(BookingActivity.this, 8);
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

    private void showSelectedBicycleInfo(int index)
    {
        BicycleInformationClass bicycle = selectedParkingStationInfo.get(index);

        TextView brand_model = (TextView) findViewById(R.id.bicycle_brand_detail);
        brand_model.setText(bicycle.getBicycleBrand() + " / " + bicycle.getBicycleModel());

        TextView station_status = (TextView) findViewById((R.id.status_station));

        String content = "Available " + bicycle.getAvailabelNumber() + " bicycles for this model";
        if (Integer.valueOf(bicycle.getAvailabelNumber()) == 1)
        {
            content = "Available " + bicycle.getAvailabelNumber() + " bicycle for this model";
        }

        station_status.setText(content);

        ImageView imageView_1 = (ImageView) findViewById(R.id.image_1);
        if (bicycle.listImageUrl.size() > 0)
        {
            setImageFromUrl(bicycle.listImageUrl.get(0), imageView_1);
        }

        ImageView imageView_2 = (ImageView) findViewById(R.id.image_2);
        if (bicycle.listImageUrl.size() > 1)
        {
            setImageFromUrl(bicycle.listImageUrl.get(1), imageView_2);
        }
    }

    private void setImageFromUrl(String _url, ImageView imageView)
    {
        try
        {
            new DownloadImageTask(imageView)
                .execute(_url);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    private void renewParkingStationInfo(int size)
    {
        selectedParkingStationInfo = new ArrayList<>(size);
        selectedBicycleIndex = 0;
    }

    private void setStationInfo()
    {
        try {
            String _stationAddress = GlobalVariable.getSelectedParkingStation().getStationAddress();
            TextView stationAddress = (TextView) findViewById(R.id.address_station);
            stationAddress.setText(_stationAddress);
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
