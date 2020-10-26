package com.example.assignment3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Menu;

import com.example.assignment3.ui.GeoFenceListener;
import com.example.assignment3.ui.reminder.ReminderFragment;
import com.example.assignment3.ui.home.HomeFragment;
import com.example.assignment3.ui.geofence.GeofenceFragment;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.nlopez.smartlocation.OnGeofencingTransitionListener;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.activity.config.ActivityParams;
import io.nlopez.smartlocation.geofencing.model.GeofenceModel;
import io.nlopez.smartlocation.geofencing.utils.TransitionGeofence;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;

public class MainActivity extends AppCompatActivity implements OnLocationUpdatedListener, GeoFenceListener, OnGeofencingTransitionListener {
    private LocationGooglePlayServicesProvider provider;
    private SmartLocation smartLocation;
    private ActivityParams.Builder activityBuilder;
    private static final int LOCATION_PERMISSION_ID = 1001;
    GeofenceModel geoFence;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static final String TAG = MainActivity.class.getSimpleName();
    private AppBarConfiguration mAppBarConfiguration;
    public GeofenceFragment geoFrag;
    public HomeFragment homeFrag;
    public ReminderFragment calFrag;
    public LatLng currPlace;
    public boolean reminder_set = false;
    public boolean place_set = false;
    public Menu menu;

    private TrackerViewModel mTrackerViewModel;
    Tracker tracker;

    MenuItem databaseInfo;
    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;

    NotificationManager notificationManager;
    private GeofencingClient geofencingClient;
    private List<Geofence> geofenceList;
    private PendingIntent geofencePendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
            return;
        }

        provider = new LocationGooglePlayServicesProvider();
        provider.setCheckLocationSettings(true);

        smartLocation = new SmartLocation.Builder(this).logging(true).build();
        activityBuilder = new ActivityParams.Builder().setInterval(5);

        getLocation();
        startLocation();

        mTrackerViewModel = new ViewModelProvider(this).get(TrackerViewModel.class);
        mTrackerViewModel.getAllTrackers().observe(this, new Observer<List<Tracker>>() {
            @Override
            public void onChanged(@Nullable final List<Tracker> trackers) {
                // Update the cached copy of the words in the adapter.
                if (trackers.size() != 0) {
                    tracker = trackers.get(0);
                } else {
                    tracker = new Tracker("Hashimoto's Tracker");
                    tracker.numMissed = 0;
                    tracker.numTracked = 1;
                    tracker.lat = 0.0;
                    tracker.lng = 0.0;
                    tracker.radius = 0;
                    tracker.hour = 0;
                    tracker.min = 0;
                    tracker.dateLastTracked = dateFormat.format(new Date(System.currentTimeMillis() - 1000L * 60L * 60L * 24L));
                    mTrackerViewModel.insert(tracker);
                }
            }
        });

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceList = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        databaseInfo = menu.findItem(R.id.action_settings);
        databaseInfo.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AlertDialog.Builder alert = new AlertDialog.Builder(
                        MainActivity.this);
                alert.setTitle("DATA");
                String min = String.valueOf(tracker.min);
                if (tracker.min < 10) {
                    min = "0" + min;
                }
                if (tracker != null) {
                    try {
                        alert.setMessage(tracker.getTracker()
                                + "\n\tDays Tracked: " + tracker.numTracked
                                + "\n\tPills Missed: " + tracker.numMissed
                                + "\n\t" + "Last tracked: " + dateFormat.parse(tracker.dateLastTracked)
                                + "\n\tLocation:" + "\n\t\tlat: " + tracker.lat
                                + "\n\t\tlng: " + tracker.lng
                                + "\n\t\tradius: " + tracker.radius + " m"
                                + "\n\t" + "Reminder Time: " + tracker.hour + ":" + min);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                alert.setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alert.create();
                alertDialog.show();
                return false;
            }
        });
        return true;
    }


    public void onUpdateMissed() {
        mTrackerViewModel.updateMissed(tracker);
        mTrackerViewModel.updateLastTracked(dateFormat.format(new Date()), tracker);
    }

    public void onUpdateTracked() {
        mTrackerViewModel.updateTracked(tracker);
        mTrackerViewModel.updateLastTracked(dateFormat.format(new Date()), tracker);
    }

    public void onUpdateLocation(double lat, double lng) {
        mTrackerViewModel.updateLocation(lat, lng, tracker);
    }

    public void onUpdateReminder(int hour, int min) {
        mTrackerViewModel.updateReminder(hour, min, tracker);
    }

    public void onUpdateGeoFenceRadius(int radius) {
        mTrackerViewModel.updateGeoFenceRadius(radius, tracker);
//        startGeoFence();
    }

    public void onSetGeofence(){
        Log.i(TAG, "In ON set GeoFence");
        startGeoFence();
    }

    public void setGeoFence() {
        Geofence g = new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("1")

                .setCircularRegion(
                        tracker.lat,
                        tracker.lng,
                        tracker.radius
                )
                .setExpirationDuration(1000000000)
                .setLoiteringDelay(200)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL)
                .build();
        Log.i(TAG, g.toString());
        Log.i(TAG, "In set GeoFence");
        geofenceList.add(g);
//        geoFence = new GeofenceModel.Builder("1")
//                .setTransition(Geofence.GEOFENCE_TRANSITION_DWELL)
//                .setLatitude(tracker.lat)
//                .setLongitude(tracker.lng)
//                .setRadius(tracker.radius)
//                .build();
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    @SuppressLint("MissingPermission")
    public void startGeoFence() {
//        if ( geoFence != null){
//            smartLocation.geofencing().remove("1").stop();
//        }
//        if (tracker.radius != 0){
//            setGeoFence();
//            smartLocation.geofencing().add(geoFence).start(this);
//        }
        setGeoFence();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
        Log.i(TAG, "In start GeoFence");
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "GeoFences Added");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed add geofence");
                    }
                });
    }

    public void stopGeoFence(){
        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "GeoFences Added");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "failed remove geofence");
                    }
                });
//        smartLocation.with(this).geofencing().stop();
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        Log.i(TAG, "HERE"+ geofencePendingIntent.toString());
        return geofencePendingIntent;
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void getLocation() {
        smartLocation.location(provider).config(LocationParams.NAVIGATION).oneFix().start(this);

    }

    private void startLocation() {
        smartLocation.location(provider).config(LocationParams.LAZY).continuous().start(this);
    }

    private void stopLocation() {
        SmartLocation.with(this).location().stop();
        stopGeoFence();
    }
    @Override
    public void onLocationUpdated(Location location) {
        currPlace = new LatLng(location.getLatitude(), location.getLongitude());
        onUpdateCurrentLocation(currPlace);
    }

    public void toggleReminder(){
        reminder_set = !reminder_set;
        if(homeFrag != null){
            homeFrag.reminder_set();
        }
    }

    public void togglePlace(){
        place_set = !place_set;
        if(homeFrag != null){
            homeFrag.place_set();
        }
    }
    @Override
    protected void onStop() {
        stopLocation();
        super.onStop();

    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocation();
    }

    @Override
    public void onUpdateCurrentLocation(LatLng latLng) {
        if(geoFrag!= null){
            geoFrag.onUpdateCurrentLocation(latLng);
        }
        currPlace = latLng;
    }

    @Override
    public void onGeofenceTransition(TransitionGeofence transitionGeofence) {
        if (geoFrag != null){
            Log.i(TAG, "IN GEOFENCEING");
            geoFrag.currStatus.setText(String.valueOf(transitionGeofence.getTransitionType()));
        }
        try {
            if(transitionGeofence.getTransitionType() == Geofence.GEOFENCE_TRANSITION_EXIT && new Date().compareTo(dateFormat.parse(tracker.dateLastTracked)) >0){
                Intent intent = new Intent(this, MainActivity.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
                PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

// build notification
// the addAction re-use the same intent to keep the example short
                Notification n  = new Notification.Builder(this)
                        .setContentTitle("Don't Forget!")
                        .setContentText("We noticed you are leaving and forgot to track your Synthroid!")
//                        .setSmallIcon(R.drawable.icon)
                        .setContentIntent(pIntent)
                        .setAutoCancel(true)
//                        .addAction(R.drawable., "Call", pIntent)
                        .addAction(R.drawable.ic_launcher_foreground, "Track", pIntent).build();


                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                notificationManager.notify(0, n);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

//    public void sendNote( String Details){
//        Intent intent = new Intent(this, MainActivity.class);
//// use System.currentTimeMillis() to have a unique ID for the pending intent
//        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
//
//// build notification
//// the addAction re-use the same intent to keep the example short
//        Notification n  = new Notification.Builder(this)
//                .setContentTitle("Don't Forget!")
//                .setContentText("We noticed you are leaving and forgot to track your Synthroid!")
////                        .setSmallIcon(R.drawable.icon)
//                .setContentIntent(pIntent)
//                .setAutoCancel(true)
////                        .addAction(R.drawable., "Call", pIntent)
//                .addAction(R.drawable.ic_launcher_foreground, "Track", pIntent).build();
//
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//        notificationManager.notify(0, n);
//    }
//    public class GeofenceBroadcastReceiver extends BroadcastReceiver {
//        private final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();
//
//        // ...
//        protected void onReceive(Context context, Intent intent) {
//            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
//            if (geofencingEvent.hasError()) {
//                String errorMessage = GeofenceStatusCodes
//                        .getStatusCodeString(geofencingEvent.getErrorCode());
//                Log.e(TAG, errorMessage);
//                return;
//            }
//
//            // Get the transition type.
//            int geofenceTransition = geofencingEvent.getGeofenceTransition();
//
//            // Test that the reported transition was of interest.
//            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
//                    geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//
//                // Get the geofences that were triggered. A single event can trigger
//                // multiple geofences.
//                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
//
//                // Get the transition details as a String.
//                String geofenceTransitionDetails = getGeofenceTransitionDetails(
//                        this,
//                        geofenceTransition,
//                        triggeringGeofences
//                );
//
//                // Send notification and log the transition details.
//                sendNote(geofenceTransitionDetails);
//                Log.i(TAG, geofenceTransitionDetails);
//            } else {
//                // Log the error.
//                Log.e(TAG, "There has been an error in broadcasting");
////                Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
////                        geofenceTransition));
//            }
//        }
//    }
//    // Create an explicit intent for an Activity in your app
//    Intent intent = new Intent(this, AlertDetails.class);
//    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//
//    NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .setContentTitle("My notification")
//            .setContentText("Hello World!")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//            // Set the intent that will fire when the user taps the notification
//            .setContentIntent(pendingIntent)
//            .setAutoCancel(true);
//
//    private void createNotificationChannel() {
//        // Create the NotificationChannel, but only on API 26+ because
//        // the NotificationChannel class is new and not in the support library
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = getString(R.string.channel_name);
//            String description = getString(R.string.channel_description);
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
//            channel.setDescription(description);
//            // Register the channel with the system; you can't change the importance
//            // or other notification behaviors after this
//            NotificationManager notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(channel);
//        }
//    }
}