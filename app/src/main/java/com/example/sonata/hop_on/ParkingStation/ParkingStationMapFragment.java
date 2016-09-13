package com.example.sonata.hop_on.ParkingStation;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sonata.hop_on.BicycleBooking.BookingActivity;
import com.example.sonata.hop_on.BicycleBooking.CurrentBookingActivity;
import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;

import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 {@link ParkingStationMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ParkingStationMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ParkingStationMapFragment extends Fragment
    implements OnMapReadyCallback,
               GoogleApiClient.ConnectionCallbacks,
               ActivityCompat.OnRequestPermissionsResultCallback,
               GoogleApiClient.OnConnectionFailedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    public static final String TAG = CurrentBookingActivity.class.getSimpleName();

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static View view;

    static Activity context;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;

    private LatLng searchLocation;
    MapView mMapView;

    TextView stationName;
    TextView stationAddress;
    TextView distance;
    TextView availableBicycles;
    TextView totalBicycles;

    Location mLastLocation = null;
    LatLng lastLocation;

    private boolean isSearchClicked = false;

    android.support.v7.widget.CardView cardView;

    private static boolean isClickedOnMarker = false;

    private OnFragmentInteractionListener mListener;

    ArrayList<ParkingStationClass> nearestParkingStations;

    public static ParkingStationMapFragment newInstance(String param1, String param2) {
        ParkingStationMapFragment fragment = new ParkingStationMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ParkingStationMapFragment() {
        // Required empty public constructor
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        mLastLocation = location;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        if (mGoogleApiClient == null)
        {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(context)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();

        }

        if (mLastLocation != null)
        {
            handleNewLocation(mLastLocation);
            lastLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            GlobalVariable.setCurrentLocation(lastLocation);
            if (mMap != null)
            {
                cameraAction(lastLocation);
            }

            getNearestParkingStation(lastLocation, isSearchClicked);
        }

        MapsInitializer.initialize(context);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(context, CONNECTION_FAILURE_RESOLUTION_REQUEST);
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
        if ( ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED )
        {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation == null) {

            }
            else {
                handleNewLocation(mLastLocation);
                lastLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                GlobalVariable.setCurrentLocation(lastLocation);
                if (mMap != null)
                {
                    cameraAction(lastLocation);
                }

                getNearestParkingStation(lastLocation, isSearchClicked);
            };
            return;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_parking_station_map, container, false);

        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        cardView          = (android.support.v7.widget.CardView) view.findViewById(R.id.cv);
        stationName       = (TextView) view.findViewById(R.id.station_name);
        stationAddress    = (TextView) view.findViewById(R.id.station_address);
        distance          = (TextView) view.findViewById(R.id.distance);
        availableBicycles = (TextView) view.findViewById(R.id.available_bicycles);
        totalBicycles     = (TextView) view.findViewById(R.id.total_bicycles);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GlobalVariable.isAvailableBicycleAtSelectedStation())
                {
                    Intent intent = new Intent(context, BookingActivity.class);
                    startActivity(intent);
                }
                else
                {
                    Toast.makeText(context, "There is no available bicycle in this station.", Toast.LENGTH_LONG).show();
                }
            }
        });

        initPlaceAutoCompleteSearch();

        return view;
    }

    private void initPlaceAutoCompleteSearch()
    {
        try
        {
            FragmentManager fragmentManager = getActivity().getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            PlaceAutocompleteFragment autocompleteFragment = new PlaceAutocompleteFragment();
            fragmentTransaction.replace(R.id.place_autocomplete_fragment, autocompleteFragment);
            fragmentTransaction.commit();

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    lastLocation = place.getLatLng();
                    String placeName = String.valueOf(place.getName());

                    cameraAction(lastLocation);
                    isSearchClicked = true;
                    getNearestParkingStation(lastLocation, isSearchClicked);
                }

                @Override
                public void onError(Status status) {
                    Log.i("OnPlaceSelected", "An error occurred: " + status);
                }
            });
            //- setUp Google Map
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        isSearchClicked = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Hide the zoom controls as the button panel will cover it.
        mMap.getUiSettings().setZoomControlsEnabled(false);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                isClickedOnMarker = true;
                setSelectedStationIndex(marker.getPosition());
                displayStationInMap(lastLocation, isSearchClicked);
                showDetailInfo();
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isClickedOnMarker == true)
                {
                    isClickedOnMarker = false;
                    cardView.setVisibility(View.INVISIBLE);
                    displayStationInMap(lastLocation, isSearchClicked);
                }
            }
        });

        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.

        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);

            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    Location myLocation = mMap.getMyLocation();

                    //+ Move camera to current device location
                    LatLng location = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    cameraAction(location);
                    //- Move camera to current device location

                    isSearchClicked = false;
                    getNearestParkingStation(location, isSearchClicked);

                    GlobalVariable.setCurrentLocation(location);
                    return true;
                }
            });

            if (mLastLocation == null)
            {
                // Add a marker in Singapore and move the camera
                LatLng SINGAPORE = new LatLng(1.290270, 103.851959);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(SINGAPORE));
            }
            else
            {
                lastLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                cameraAction(lastLocation);
                getNearestParkingStation(lastLocation, isSearchClicked);
            }
        }
    }

    private void displayStationInMap(LatLng location, boolean myLocation)
    {
        mMap.clear();

        for(int i = 0; i < nearestParkingStations.size(); i++)
        {
            final ParkingStationClass station = nearestParkingStations.get(i);
            if (isClickedOnMarker == true &&
                    GlobalVariable.isSelectedParkingStation(station.getLocation()) == true)
            {
                mMap.addMarker(new MarkerOptions()
                        .position(station.getLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_out_of_order_station)));
            }
            else
            {
                if (Integer.valueOf(station.getNumberOfAvailableBicycle()) == 0)
                {
                    mMap.addMarker(new MarkerOptions()
                            .position(station.getLocation())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_not_available_bicycle)));
                }
                else
                {
                    mMap.addMarker(new MarkerOptions()
                            .position(station.getLocation())
                            .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_available_station)));
                }
            }
        }

        if (myLocation == true)
        {
            mMap.addMarker(new MarkerOptions()
                    .position(lastLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        }
    }

    private void setSelectedStationIndex(LatLng position)
    {
        for(int i = 0; i < nearestParkingStations.size(); i++)
        {
            ParkingStationClass station = nearestParkingStations.get(i);
            if ((station.location.latitude == position.latitude) &&
                    (station.location.longitude == position.longitude)) {
                GlobalVariable.setSelectedParkingStation(station);
                return;
            }
        }
    }

    void showDetailInfo()
    {
        try
        {
            ParkingStationClass station = GlobalVariable.getSelectedParkingStation();

            stationName.setText(station.getStationName());
            stationAddress.setText(station.getStationAddress());
            distance.setText(station.getDistance());
            availableBicycles.setText(station.getAvailableBicycles());
            totalBicycles.setText(station.getTotalBicycles());
            cardView.setVisibility(View.VISIBLE);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void renewNearestParkingStationData(int size)
    {
        nearestParkingStations = new ArrayList<>(size);
    }

    private void getNearestParkingStation(final LatLng location, final boolean myLocation)
    {
        Preferences.showLoading(context, "Parking Station", "Getting nearest parking station...");

        JsonObject loc = new JsonObject();
        try
        {
            loc.addProperty("latitude", location.latitude);
            loc.addProperty("longitude", location.longitude);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            SharedPreferences pref = getActivity().getSharedPreferences("HopOn_pref", 0);
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

                            displayStationInMap(location, myLocation);
                        }
                        else
                        {
                            if (messageCode == 500) // SERVER FAILED
                            {
                                Notification.showMessage(context, 1);
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

    private void cameraAction(LatLng targetLocation)
    {
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());

        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        // Construct a CameraPosition focusing on Target Location and animate the camera to that position.
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(targetLocation)     // Sets the center of the map to Target Location
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    public void onStart() {
        super.onStart();
        if( mGoogleApiClient != null )
            mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if( mGoogleApiClient != null && mGoogleApiClient.isConnected() ) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}