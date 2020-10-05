package com.example.assignment1;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.nlopez.smartlocation.BuildConfig;
import io.nlopez.smartlocation.OnActivityUpdatedListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.OnReverseGeocodingListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

/**
 * An activity that tracks location and activity
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, OnLocationUpdatedListener, OnActivityUpdatedListener, SensorEventListener, StepListener {
    private LocationGooglePlayServicesProvider provider;
    private SmartLocation smartLocation;
    private  ActivityParams.Builder activityBuilder;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final long UPDATE_INTERVAL = 1000; // Every 60 seconds.


    public DetectedActivity detectedActivity;
    private GoogleMap map;
    private CameraPosition cameraPosition;
    private MusicManager musicManager;

    // The entry point to the Places API.
    private PlacesClient placesClient;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient fusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] likelyPlaceNames;
    private String[] likelyPlaceAddresses;
    private List[] likelyPlaceAttributions;
    private LatLng[] likelyPlaceLatLngs;

    // Initial Line
    private ArrayList<LatLng> points;
    Polyline line;

    private ExtendedFloatingActionButton export;
    private ExtendedFloatingActionButton musicToggle;
    private ExtendedFloatingActionButton mRequestUpdatesButton;
    private ExtendedFloatingActionButton mRemoveUpdatesButton;
    private ExtendedFloatingActionButton resetTracking;

    Context context;

    Handler handler;
    private TextView trackTextView;
    private TextView percentageTextView;
    private static final int LOCATION_PERMISSION_ID = 1001;


    private StepDetector simpleStepDetector;
    private SensorManager sensorManager;
    private Sensor accel;
    private int numSteps;

    private float distance;
    private long startTime;
    private long msOffset = 0;
    private Handler tHandler = new Handler();
    private Runnable tRunnable = new Runnable(){
        @Override
        public void run() {
            long ms = System.currentTimeMillis() - startTime - msOffset;
            int s = (int) (ms/1000);
            int m = s/60;
            int h = m/60;
            m = m%60;
            s = s%60;

            updateTimer(h, m, s);
            tHandler.postDelayed(this, 500);

        }
    };


    int s = 0;
    int m = 0;
    int h = 0;
    String timerString = "%d:%02d:%02d";
    int totalActivitiesTracked = 0;
    double[] activityPercentages;
    int topActivity;
    int secondActivity;
    int thirdActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }


        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_map);


        /* Step Counter Objects */
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        simpleStepDetector = new StepDetector();
        simpleStepDetector.registerListener(this);

        /* Music Manager Objects */
        musicToggle = (ExtendedFloatingActionButton)findViewById(R.id.musicToggle);

        /* Export Objects */
        export = (ExtendedFloatingActionButton)findViewById(R.id.export);

        /* Tracking Objects */
        mRequestUpdatesButton = (ExtendedFloatingActionButton)findViewById(R.id.request_updates_button);
        mRemoveUpdatesButton = (ExtendedFloatingActionButton)findViewById(R.id.remove_updates_button);
        resetTracking = (ExtendedFloatingActionButton)findViewById(R.id.reset_tracking);

        trackTextView = (TextView) findViewById(R.id.trackTextView);
        trackTextView.setText("Location here");
        percentageTextView= (TextView) findViewById(R.id.percentageTextView);
        percentageTextView.setText("Activity here");

        detectedActivity = new DetectedActivity(DetectedActivity.UNKNOWN, 0);
        context = mRequestUpdatesButton.getContext();
        handler = new Handler();


        // init points
        points = new ArrayList<LatLng>();

        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        musicManager = new MusicManager();
        musicManager.startMusicManager(this);

        mRequestUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTrack();
            }
        });

        mRemoveUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTrack();
            }
        });

        resetTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetTrack();
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        activityPercentages = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0};


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            return;
        }


        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        smartLocation = new SmartLocation.Builder(this).logging(true).build();
        activityBuilder = new ActivityParams.Builder().setInterval(0);

        getLocation();
        mRemoveUpdatesButton.setEnabled(false);
        resetTracking.setEnabled(false);


    }

    private void updateTimer(int hours, int minutes, int seconds){
        h = hours;
        s = seconds;
        m = minutes;
        showLastActivity();
        updateTrackTextView();
        updatePercentageTextView();
    }

    private void setTimerOffset(){
        msOffset = ((h*60 + m)*60 + s)*1000;
    }
    private void startTrack(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            return;
        }
        startLocation();
        sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);

        /* Toggle Button Enabled */
        mRemoveUpdatesButton.setEnabled(true);
        mRequestUpdatesButton.setEnabled(false);
        resetTracking.setEnabled(true);

        startTime = System.currentTimeMillis();
        tHandler.postDelayed(tRunnable, 0);

        musicManager.toggletracking(true);

    }

    private void stopTrack(){
        stopLocation();
        sensorManager.unregisterListener(MainActivity.this);

        /* Toggle Button Enabled */
        mRemoveUpdatesButton.setEnabled(false);
        mRequestUpdatesButton.setEnabled(true);
        resetTracking.setEnabled(true);

        tHandler.removeCallbacks(tRunnable);

        musicManager.toggletracking(false);
    }

    private void resetTrack(){
        stopTrack();
        points.clear();

        distance = (float)0;
        numSteps = 0;
        h = 0;
        m = 0;
        s = 0;
        updateTrackTextView();
        percentageTextView.setText("Top 3 Activities");

        int i = 0;
        while(i<activityPercentages.length){
            activityPercentages[i] = 0;
            Log.i(TAG, "HERE" + i);
            i++;
        }
        resetTracking.setEnabled(false);
    }
    private void getLocation() {
        smartLocation.location(provider).config(LocationParams.NAVIGATION).oneFix().start(this);

    }
    private void startLocation() {
        smartLocation.location(provider).config(LocationParams.NAVIGATION).continuous().start(this);
        smartLocation.activity().config(activityBuilder.build()).start(this);
    }

    private void stopLocation() {
        SmartLocation.with(this).location().stop();
        SmartLocation.with(this).activity().stop();
    }
    private void getLengthOfMovement(LatLng last, LatLng next){
        float res[];
        res = new float[3];
        Location.distanceBetween(last.latitude, last.longitude, next.latitude, next.longitude, res);
        distance = distance + res[0];

    }

    private void redrawTrack(){
        Log.i(TAG, "Redrawing Track");
        line.setPoints(points);
    }

    private void showLocation(Location location) {
        if (location != null) {
            final String text = String.format("Latitude %.6f, Longitude %.6f",
                    location.getLatitude(),
                    location.getLongitude());

            LatLng next = new LatLng(location.getLatitude(),
                    location.getLongitude());
            /* If new track */
            if (points.size() == 0) {
                PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                line = map.addPolyline(options.clickable(true).add(new LatLng(location.getLatitude(),
                        location.getLongitude())));
            } else {
                LatLng last = points.get(points.size() - 1);

                if (!last.equals(next)) {
                    getLengthOfMovement(last, next);
                }
            }


            points.add(next);
            redrawTrack();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    next, DEFAULT_ZOOM));
        }
    }

    private void showActivity(DetectedActivity activity) {
        Log.i(TAG, "LOGGING ACTIVITY UPDATE");
        if (activity != null) {
            if (activity != detectedActivity){
                musicManager.changeActivityState(getPlaylist(activity.getType()));
            }
            detectedActivity = activity;
            addNewActivity(activity);
        }
    }

    public void addNewActivity(DetectedActivity activity){
        totalActivitiesTracked ++;
        activityPercentages[activity.getType()]  = activityPercentages[activity.getType()] +1 ;


    }
    @Override
    public void onActivityUpdated(DetectedActivity detectedActivity) {
        showActivity(detectedActivity);

        /* Update UI */
        updateTrackTextView();
        updatePercentageTextView();
    }

    @Override
    public void onLocationUpdated(Location location) {
        showLocation(location);

        /* Update UI */
        updateTrackTextView();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepDetector.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    @Override
    public void step(long timeNs) {
        numSteps++;
        updateTrackTextView();
    }

    public void getThreeLargest(double arr[], int arr_size){
        double a, b,c;
        int i, a_i, b_i, c_i;

        /* Check array has more than 3*/
        if (arr_size< 3){
            return;
        }
        a = b = c = Integer.MIN_VALUE;
        a_i = b_i = c_i = 0;
        for (i = 0; i<arr_size; i++){

            /* if curr arr[i] is larger than a*/
            if( arr[i]> a){
                c = b;
                c_i = b_i;
                b = a;
                b_i = a_i;
                a = arr[i];
                a_i = i;
            }

            /* if curr arr[i] is between a and b*/
            else if( arr[i]> b){
                c = b;
                c_i = b_i;
                b = arr[i];
                b_i = i;
            }
            /* if curr arr[i] is between a and b*/
            else if( arr[i]> c){
                c = arr[i];
                c_i = i;
            }
        }
        topActivity = a_i;
        secondActivity = b_i;
        thirdActivity = c_i;
        Log.i(TAG, "activities Array: " + String.valueOf(a_i)+ String.valueOf(b_i) + String.valueOf(c_i));


    }
    public void updatePercentageTextView(){
        Log.i(TAG, "activities Array: " +Arrays.toString(activityPercentages));
        getThreeLargest(activityPercentages, activityPercentages.length);
        percentageTextView.setText(String.format("Top 3 Activities:\n%.1f%% - %s\n%.1f%% - %s\n%.1f%% - %s",
                100* activityPercentages[topActivity]/totalActivitiesTracked, getNameFromType(topActivity),
                100* activityPercentages[secondActivity]/totalActivitiesTracked,getNameFromType(secondActivity),
                100* activityPercentages[thirdActivity]/totalActivitiesTracked,getNameFromType(thirdActivity)));
    }
    public void updateTrackTextView(){
        trackTextView.setText(String.format("Run Track:\nTime: %s\nDistance: %f meters\nSteps: %d\nActivity: %s",
                String.format(timerString, h, m, s),
                distance, numSteps, getNameFromType(detectedActivity.getType())));
    }

    public enum userActivityState{
        UNK{
            @Override
            public String playlist() {
                return "spotify:playlist:3yiX3ROHK4vo82pR6BO8eW";
            }
        },
        WALKING{
            @Override
            public String playlist() {
                return "spotify:playlist:3yiX3ROHK4vo82pR6BO8eW";
            }

        },
        RUNNING{
            @Override
            public String playlist() {
                return "spotify:playlist:4cgeOaRCHDkVDQPaDrRQFR";
            }

        },
        DRIVING{
            @Override
            public String playlist() {
                return "spotify:playlist:1Pzg5ub8FkriYcBcYGBV5t";
            }

        };

        public abstract String playlist();
    }

    private void showLastActivity() {
        DetectedActivity activity = SmartLocation.with(this).activity().getLastActivity();
        if (activity != null) {
            detectedActivity = activity;
            addNewActivity(activity);
        }
    }
    private void showLast() {
        Location lastLocation = SmartLocation.with(this).location().getLastLocation();
        if (lastLocation != null) {
//            trackTextView.setText(
//                    String.format("[From Cache] Latitude %.6f, Longitude %.6f",
//                            lastLocation.getLatitude(),
//                            lastLocation.getLongitude())
//            );

        }
        DetectedActivity detectedActivity = SmartLocation.with(this).activity().getLastActivity();
        if (detectedActivity != null) {
//            mactivity.setText(
//                    String.format("[From Cache] Activity %s with %d%% confidence",
//                            getNameFromType(detectedActivity),
//                            detectedActivity.getConfidence())
//            );
        }
    }


    private String getNameFromType(int activity) {
        switch (activity) {
            case DetectedActivity.IN_VEHICLE:
                return "Driving";
            case DetectedActivity.ON_BICYCLE:
                return "Biking";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.WALKING:
                return "Walking";
            case DetectedActivity.RUNNING:
                return "Running";
            default:
                return "Unknown";
        }
    }

    private String getPlaylist(int activity) {
        switch (activity) {
            case DetectedActivity.IN_VEHICLE:
                return "spotify:playlist:37i9dQZF1DWWMOmoXKqHTD";
            case DetectedActivity.ON_BICYCLE:
                return "spotify:playlist:1AQCXgGj0rBvhyGFA8EVen";
            case DetectedActivity.ON_FOOT:
                return "spotify:playlist:0whX9iLOo6Bf46EBdPaT6b";
            case DetectedActivity.STILL:
                return "spotify:track:74JzwF5NaUUpo9alw3WDVD";
            case DetectedActivity.UNKNOWN:
                return "spotify:playlist:0NUtHPgeWm833NU14csQZi";
            case DetectedActivity.TILTING:
                return "spotify:playlist:0xNA9Ku68AfcXdzKJisT8z";
            case DetectedActivity.WALKING:
                return "spotify:playlist:0r4VvKXdMZEygazSd3Z7xn";
            case DetectedActivity.RUNNING:
                return "spotify:playlist:28CKLsz35YeJ4bJUOFqXIS";
            default:
                return "spotify:playlist:0NUtHPgeWm833NU14csQZi";
        }
    }


    public void musicPrevious(View view){
        musicManager.previous();
    }

    public void musicNext(View view){
        musicManager.next();
    }

    public void musicToggle(View view){
        if (musicManager.mState == MusicManager.MusicState.PLAYING){
            musicToggle.setIcon(getResources().getDrawable(R.drawable.ic_baseline_play_arrow_24));
            musicManager.pause();
        }
        else{
            musicManager.start();
            musicToggle.setIcon(getResources().getDrawable(R.drawable.ic_baseline_pause_24));
        }

    }


    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }

    /**
     * Handles a click on the menu option to get a place.
     * @param item The menu item to handle.
     * @return Boolean.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.option_get_place) {
            showCurrentPlace();
        }
        return true;
    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        Log.d(TAG, "in MAP READY");
        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        this.map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = infoWindow.findViewById(R.id.title);
                title.setText(marker.getTitle());

                TextView snippet = infoWindow.findViewById(R.id.snippet);
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        showLast();
    }


    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            Log.d(TAG, "in get location");

                            if (lastKnownLocation != null) {
                                points.add(new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()));

                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));


                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
//                    startLocation();
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (map == null) {
            return;
        }

        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS,
                    Place.Field.LAT_LNG);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.newInstance(placeFields);

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final
            Task<FindCurrentPlaceResponse> placeResult =
                    placesClient.findCurrentPlace(request);
            placeResult.addOnCompleteListener (new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FindCurrentPlaceResponse likelyPlaces = task.getResult();

                        // Set the count, handling cases where less than 5 entries are returned.
                        int count;
                        if (likelyPlaces.getPlaceLikelihoods().size() < M_MAX_ENTRIES) {
                            count = likelyPlaces.getPlaceLikelihoods().size();
                        } else {
                            count = M_MAX_ENTRIES;
                        }

                        int i = 0;
                        likelyPlaceNames = new String[count];
                        likelyPlaceAddresses = new String[count];
                        likelyPlaceAttributions = new List[count];
                        likelyPlaceLatLngs = new LatLng[count];

                        for (PlaceLikelihood placeLikelihood : likelyPlaces.getPlaceLikelihoods()) {
                            // Build a list of likely places to show the user.
                            likelyPlaceNames[i] = placeLikelihood.getPlace().getName();
                            likelyPlaceAddresses[i] = placeLikelihood.getPlace().getAddress();
                            likelyPlaceAttributions[i] = placeLikelihood.getPlace()
                                    .getAttributions();
                            likelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                            i++;
                            if (i > (count - 1)) {
                                break;
                            }
                        }

                        // Show a dialog offering the user the list of likely places, and add a
                        // marker at the selected place.
                        MainActivity.this.openPlacesDialog();
                    }
                    else {
                        Log.e(TAG, "Exception: %s", task.getException());
                    }
                }
            });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");

            // Add a default marker, because the user hasn't selected a place.
            map.addMarker(new MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(defaultLocation)
                    .snippet(getString(R.string.default_info_snippet)));

            // Prompt the user for permission.
            getLocationPermission();
        }
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = likelyPlaceLatLngs[which];
                String markerSnippet = likelyPlaceAddresses[which];
                if (likelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + likelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.
                map.addMarker(new MarkerOptions()
                        .title(likelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet));

                // Position the map's camera at the location of the marker.
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(likelyPlaceNames, listener)
                .show();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    @Override
    protected void onStop() {
        musicManager.stopMusicManager();
        stopLocation();
        super.onStop();

    }

    @Override
    protected void onStart() {
        super.onStart();
        musicManager.startMusicManager(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocation();
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void export(View view){

        stopTrack();


        //generate data
        StringBuilder data = new StringBuilder();
        data.append("Movement Data");
        data.append("\nField, Data,");
        data.append("\nTime Elapsed, " + String.format(timerString, h,m,s) +",");
        data.append("\nDistance Traveled in meters, " + String.valueOf(distance) +",");
        data.append("\nSteps Tracked, " + String.valueOf(numSteps) +",");
        data.append("\n\n Activities, Percentage of time performed,");
        for(int i = 0; i< activityPercentages.length; i++){
            data.append("\n" + getNameFromType(i) + ", " +  String.valueOf(100*activityPercentages[i]/totalActivitiesTracked) + "%,");
        }


        try{
            //saving the file into device
            FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            //exporting
            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context, "com.example.exportcsv.fileprovider", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            LocalDateTime now = LocalDateTime.now();
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Activity_Data_"+ now + ".csv");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Export Sensor Data"));
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}