package com.example.sonata.hop_on.Feedback;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sonata.hop_on.BicycleBooking.BookingActivity;
import com.example.sonata.hop_on.BicycleBooking.CurrentBookingActivity;
import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.NavigationDrawer.NavigationDrawerActivity;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.google.android.gms.vision.text.Text;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.altbeacon.beacon.Beacon;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends AppCompatActivity {

    private boolean issues[] = new boolean[5];

    CheckBox cb1;
    CheckBox cb2;
    CheckBox cb3;
    CheckBox cb4;
    CheckBox cb5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        this.setTitle("Feedback");

        RatingBar ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP);

        TextView bookingIdText = (TextView) findViewById(R.id.booking_id_detail);
        bookingIdText.setText(GlobalVariable.currentBookingInfo.getBooking_id());

        for(int i = 0; i < 5; i++)
        {
            issues[i] = false;
        }

        cb1 = (CheckBox) findViewById(R.id.check_box_1);
        cb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb1.isChecked())
                {
                    issues[0] = true;
                }
                else {
                    issues[0] = false;
                }
            }
        });

        cb2 = (CheckBox) findViewById(R.id.check_box_2);
        cb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb2.isChecked())
                {
                    issues[1] = true;
                }
                else {
                    issues[1] = false;
                }
            }
        });

        cb3 = (CheckBox) findViewById(R.id.check_box_3);
        cb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb3.isChecked())
                {
                    issues[2] = true;
                }
                else {
                    issues[2] = false;
                }
            }
        });

        cb4 = (CheckBox) findViewById(R.id.check_box_4);
        cb4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb4.isChecked())
                {
                    issues[3] = true;
                }
                else {
                    issues[3] = false;
                }
            }
        });

        cb5 = (CheckBox) findViewById(R.id.check_box_5);
        cb5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb5.isChecked())
                {
                    issues[4] = true;
                }
                else {
                    issues[4] = false;
                }
            }
        });

        Button btn_submit = (Button) findViewById(R.id.submit_button);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFeedbackToServer();
            }
        });
    }

    public void sendFeedbackToServer()
    {
        Preferences.showLoading(this, "Feedback", "Sending feedback to server...");

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);

        JsonObject feedback = new JsonObject();
        try
        {
            feedback.addProperty("rentalId", GlobalVariable.currentBookingInfo.getRentalId());

            JsonArray listIssue = new JsonArray();
            for(int i = 0; i < 5; i++)
                if (issues[i] == true)
                {
                    listIssue.add(i+1);
                }
            feedback.add("listIssue", listIssue);

            EditText commentText = (EditText) findViewById(R.id.input_comment);
            String comment = commentText.getText().toString();
            feedback.addProperty("comment", comment);

            RatingBar ratingBar = (RatingBar) findViewById(R.id.rating_bar);
            float rating = ratingBar.getRating();
            feedback.addProperty("rating", rating);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        Call<ResponseBody> call = client.getFeedback(feedback);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Preferences.dismissLoading();
                    int messageCode = response.code();

                    if (messageCode == 200)
                    {
                        Toast.makeText(getBaseContext(), "Feedback saved! Thank you for spending your time with Hop On!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(FeedbackActivity.this, NavigationDrawerActivity.class);
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
                Intent intent = new Intent(FeedbackActivity.this, NavigationDrawerActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
