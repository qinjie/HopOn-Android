package com.example.sonata.hop_on.ParkingStation;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sonata.hop_on.GlobalVariable.GlobalVariable;
import com.example.sonata.hop_on.Notification.Notification;
import com.example.sonata.hop_on.Preferences;
import com.example.sonata.hop_on.R;
import com.example.sonata.hop_on.ServiceGenerator.ServiceGenerator;
import com.example.sonata.hop_on.ServiceGenerator.StringClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
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
    implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    static Activity context;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;

    private LatLng searchLocation;
    MapView mMapView;

    private OnFragmentInteractionListener mListener;

    public static ParkingStationMapFragment newInstance(String param1, String param2) {
        ParkingStationMapFragment fragment = new ParkingStationMapFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ParkingStationMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

         mGoogleApiClient = new GoogleApiClient
                .Builder(context)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        MapsInitializer.initialize(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parking_station_map, container, false);

        mMapView = (MapView) view.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

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
                    searchLocation = place.getLatLng();
                    String placeName = String.valueOf(place.getName());

                    cameraAction(searchLocation);

                    mMap.addMarker(new MarkerOptions()
                            .position(searchLocation)
                            .title(placeName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
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
                return false;
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

                    GlobalVariable.setCurrentLocation(location);

                    getNearestParkingStation(location);

                    return true;
                }
            });

            // Add a marker in Singapore and move the camera
            LatLng SINGAPORE = new LatLng(1.290270, 103.851959);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(SINGAPORE));
        }
    }

    private void displayStationInMap()
    {
        mMap.clear();

        for(int i = 0; i < GlobalVariable.nearestParkingStations.size(); i++)
        {
            ParkingStationClass station = GlobalVariable.nearestParkingStations.get(i);
            if (Integer.valueOf(station.getNumberOfAvailableBicycle()) == 0)
            {
                mMap.addMarker(new MarkerOptions()
                        .position(station.getLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_out_of_order_station)));
            }
            else
            {
                mMap.addMarker(new MarkerOptions()
                        .position(station.getLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_available_station)));
            }
        }
    }

    private void getNearestParkingStation(LatLng location)
    {
        Preferences.showLoading(context, "Parking Station", "Getting nearest parking station...");

        StringClient client = ServiceGenerator.createService(StringClient.class);

        JsonObject loc = new JsonObject();
        try
        {
            loc.addProperty("latitude", location.latitude);
            loc.addProperty("longitude", location.longitude);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        Call<ResponseBody> call = client.getNearestParkingStations(loc);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    Preferences.dismissLoading();
                    int messageCode = response.code();
                    if (messageCode == 200) // SUCCESS
                    {
                        JSONArray data =  new JSONArray(response.body().string());
                        for(int i = 0 ; i < data.length(); i++) {
                            JSONObject station = (JSONObject) data.get(i);

                            String latitude = station.getString("latitude");
                            String longitude = station.getString("longitude");
                            LatLng loc = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));

                            JSONObject distance = station.getJSONObject("distance");

                            ParkingStationClass stationInfo = new ParkingStationClass(
                                    loc,
                                    station.getString("name"),
                                    station.getString("address"),
                                    station.getString("bicycle_count"),
                                    station.getString("available_bicycle"),
                                    distance.getString("text")
                            );

                            GlobalVariable.renewNearestParkingStationData();
                            GlobalVariable.addDataToNearestParkingStationList(stationInfo);
                        }

                        displayStationInMap();
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