package com.example.assignment3.ui.geofence;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.assignment3.MainActivity;
import com.example.assignment3.R;
import com.example.assignment3.ui.GeoFenceListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GeofenceFragment extends Fragment implements GeoFenceListener {
    MapView mMapView;
    private GoogleMap googleMap;
    private GeofenceViewModel slideshowViewModel;
    private static final String TAG = GeofenceFragment.class.getSimpleName();
    private LatLng currLocation;
    private Button setGeoFence;

    private LatLng geoFenceLocation;
    private Marker geoFenceMarker;
    private Circle geoFenceCircle;
    private int radius = 1000;
    private SeekBar seekBar;
    private int seekbarMin = 10;
    private int seekbarMax = 100;

    private TextView currGeoFense;
    public TextView currStatus;

    MainActivity activity;
    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        slideshowViewModel =
                ViewModelProviders.of(this).get(GeofenceViewModel.class);
        View root = inflater.inflate(R.layout.fragment_slideshow, container, false);

        activity = (MainActivity) getActivity();
        activity.geoFrag = this;
        mMapView = (MapView) root.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                UiSettings mapUi = googleMap.getUiSettings();
                mapUi.setZoomControlsEnabled(true);

//                // For showing a move to my location button
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
                googleMap.setMyLocationEnabled(true);
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng arg0) {
                        if (geoFenceLocation != null){
                            removeGeoFence();
                        }
                        setGeoFenceLocation(arg0);
                        geoFenceMarker = googleMap.addMarker(new MarkerOptions().position(arg0));
                        geoFenceCircle = googleMap.addCircle(new CircleOptions().center(arg0).radius(radius));
                        android.util.Log.i("onMapClick", "Horray!");
                    }
                });

//                // For zooming automatically to the location of the marker
                if (activity.currPlace != null){
                    currLocation = activity.currPlace;
                    CameraPosition cameraPosition = new CameraPosition.Builder().target(currLocation).zoom(12).build();
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

            }
        });
        final TextView textView = root.findViewById(R.id.text_slideshow);
        slideshowViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        seekBar = root.findViewById(R.id.radiusBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                setGeoFenceRadius(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        TextView seekbarMinTextView = root.findViewById(R.id.seekbarMin);
        seekbarMinTextView.setText(String.valueOf(seekbarMin) + " m");
        TextView seekbarMaxTextView = root.findViewById(R.id.seekbarMax);
        seekbarMaxTextView.setText(String.valueOf(seekbarMax) + " m");

        setGeoFence = (Button) root.findViewById(R.id.setGeoFence);
        setGeoFence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.togglePlace();
                activity.startGeoFence();
            }
        });
        Log.i(TAG, "In the geofence");

        currStatus = root.findViewById(R.id.currentStatus);
//        currGeoFense = root.findViewById(R.id.currGeoFence);
//        seekbarMaxTextView.setText("Click a location for Location-Based Notifications");

//        MainActivity act = (MainActivity) getActivity();

//        CameraPosition cameraPosition = new CameraPosition.Builder().target(act.pls).zoom(12).build();
//        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        Log.i(TAG, "In the geofence: activty"+ act.toString() + pls.toString());


        activity.getLocation();
        return root;
    }
    private void setGeoFenceLocation(LatLng loc){
        geoFenceLocation = loc;
        activity.onUpdateLocation(loc.latitude, loc.longitude);
    }
    private void setGeoFenceRadius(int i){
        if (i== 0){
            radius = 10;
        }
        radius = (int)(10+(i*.9));

        if (geoFenceCircle != null && geoFenceMarker != null){
            geoFenceCircle.setRadius(radius);
//            setCurrGeoFenceText();
        }
        else if (geoFenceMarker == null || geoFenceCircle == null){
            makeCurrLocationGeoFence();
        }
        activity.onUpdateGeoFenceRadius(radius);

    }

    private void makeCurrLocationGeoFence(){
        if(geoFenceLocation != null){
            removeGeoFence();
        }
        setGeoFenceLocation(currLocation);
        geoFenceMarker = googleMap.addMarker(new MarkerOptions().position(currLocation));
        geoFenceCircle = googleMap.addCircle(new CircleOptions().center(currLocation).radius(radius));
    }
    private void removeGeoFence(){
        geoFenceCircle.remove();
        geoFenceMarker.remove();
        geoFenceLocation = null;
    }

    private void setCurrGeoFenceText(){
        currGeoFense.setText("Current Chosen Location: \n\t"
                        + "Latitude: " + String.valueOf(geoFenceLocation.latitude) + "\n\t"
                        + "Longitude: " + String.valueOf(geoFenceLocation.longitude) + "\n\t"
                        + "Radius: " + String.valueOf(radius) + " meters");
    }
    @Override
    public void onAttach(Context context ) {
        super.onAttach(context);

    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onUpdateCurrentLocation(LatLng latLng) {

        if(currLocation == null){
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Marker Title").snippet("Marker Description"));

            // For zooming automatically to the location of the marker
            CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(12).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        currLocation = latLng;
        Log.i(TAG, "Here: " + String.valueOf(latLng));
    }
}