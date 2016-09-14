package com.example.sonata.hop_on.NavigationDrawer;

import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sonata.hop_on.BicycleBooking.BookingInformationClass;
import com.example.sonata.hop_on.BicycleBooking.CurrentBookingActivity;
import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.LoanHistory.CustomLoanHistoryAdapter;
import com.example.sonata.hop_on.LoanHistory.LoanHistoryFragment;
import com.example.sonata.hop_on.ParkingStation.ParkingStationListActivity;
import com.example.sonata.hop_on.ParkingStation.ParkingStationMapFragment;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.example.sonata.hop_on.UserProfile.ProfileFragment;
import com.example.sonata.hop_on.UserProfile.UserProfile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationDrawerActivity extends ActionBarActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;

    private ExpandableListView mExpandableListView;
    private ExpandableListAdapter mExpandableListAdapter;
    private List<String> mExpandableListTitle;
    private Map<String, List<String>> mExpandableListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        initExpandableList();
        getUserProfile();

        //+ Load default fragment
        android.app.Fragment fragment = new ParkingStationMapFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        //- Load default fragment
    }

    private void initExpandableList()
    {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle = getTitle().toString();

        mExpandableListView = (ExpandableListView) findViewById(R.id.navList);

        mExpandableListData = ExpandableListDataSource.getData(this);
        mExpandableListTitle = new ArrayList(mExpandableListData.keySet());

        addDrawerItems();
        setupDrawer();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void getUserProfile()
    {
        Preferences.showLoading(NavigationDrawerActivity.this, "User Information", "Loading data from server...");

        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
        String auCode = pref.getString("authorizationCode", null);

        StringClient client = ServiceGenerator.createService(StringClient.class, auCode);

        Call<ResponseBody> call = client.getUserProfile();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try{
                    Preferences.dismissLoading();
                    int messageCode = response.code();

                    if (messageCode == 200)
                    {
                        JSONObject data = new JSONObject(response.body().string());
                        UserProfile userProfile = new UserProfile(data);
                        GlobalVariable.setUserProfile(userProfile);

                        // Customize header view
                        LayoutInflater inflater = getLayoutInflater();
                        final View listHeaderView = inflater.inflate(R.layout.nav_header, null, false);

                        TextView textName = (TextView) listHeaderView.findViewById(R.id.fullname);
                        textName.setText(GlobalVariable.getUserProfile().getUserName());

                        mExpandableListView.addHeaderView(listHeaderView);
                    }
                    else if (messageCode == 400)
                    {

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

    private void addDrawerItems() {
        mExpandableListAdapter = new CustomExpandableListAdapter(this, mExpandableListTitle, mExpandableListData);
        mExpandableListView.setAdapter(mExpandableListAdapter);
        mExpandableListView.setGroupIndicator(null);

        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (groupPosition != 1) {
                    getSupportActionBar().setTitle(mExpandableListTitle.get(groupPosition).toString());
                }

                android.app.Fragment fragment = null;

                switch (groupPosition) {
                    case 0: // Parking Station Map
                        fragment = new ParkingStationMapFragment();
                        break;
                    case 1: // Current Booking
                        SharedPreferences pref = getSharedPreferences("HopOn_pref", 0);
                        String bookingStatus = pref.getString("bookingStatus", null);
                        if (bookingStatus == null || bookingStatus.compareTo(GlobalVariable.FREE) == 0)
                        {
                            Toast.makeText(getBaseContext(), "You are not booking any bicycle at the moment!", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Intent intent = new Intent(NavigationDrawerActivity.this, CurrentBookingActivity.class);
                            startActivity(intent);
                        }
                        return true;
                    case 2: // History
                        fragment = new LoanHistoryFragment();
                        break;
                    case 3: // User Profile
                        fragment = new ProfileFragment();
                        break;
                    case 4: // Exit
                        finish();
                        System.exit(0);
                }

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();

                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(NavigationDrawerActivity.this, ParkingStationListActivity.class);
            startActivity(intent);
            return true;
        }

        // Activate the navigation drawer toggle
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
