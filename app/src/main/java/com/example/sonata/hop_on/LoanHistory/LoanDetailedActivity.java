package com.example.sonata.hop_on.LoanHistory;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.sonata.hop_on.BicycleBooking.BookingInformationClass;
import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.R;

public class LoanDetailedActivity extends AppCompatActivity {

    private BookingInformationClass loanRecord;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_detailed);

        try
        {
            loanRecord = GlobalVariable.getSelectedLoanRecord();

            TextView bookingIdDetail = (TextView) findViewById(R.id.booking_id_detail);
            bookingIdDetail.setText(loanRecord.getBooking_id());

            TextView bicycleBrand = (TextView) findViewById(R.id.bicycle_brand);
            bicycleBrand.setText(loanRecord.getBicycleInfo());

            TextView bicycleSerial = (TextView) findViewById(R.id.bicycle_brand_detail);
            bicycleSerial.setText(loanRecord.getBicycleSerial());

            TextView pickUpAddress = (TextView) findViewById(R.id.pick_up_station_address);
            pickUpAddress.setText(loanRecord.getPickUpStationAddress());

            TextView bookedTime = (TextView) findViewById(R.id.booked_time_detail);
            bookedTime.setText(loanRecord.getBookedTime());

            TextView pickUpTime = (TextView) findViewById(R.id.pick_up_time_detail);
            pickUpTime.setText(loanRecord.getPickUpTime());

            TextView returnAddress = (TextView) findViewById(R.id.return_station_address);
            returnAddress.setText(loanRecord.getReturnStationAddress());

            TextView returnedTime = (TextView) findViewById(R.id.return_time_detail);
            returnedTime.setText(loanRecord.getReturnTime());

            TextView duration = (TextView) findViewById(R.id.loan_duration_detail);
            duration.setText(loanRecord.getDuration());

        } catch (Exception e)
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
