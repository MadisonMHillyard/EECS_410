package com.example.assignment3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;


public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    // ...
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

//            if()

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            sendNote(geofenceTransitionDetails, context );
            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, "There has been an error in broadcasting");
//                Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
//                        geofenceTransition));
        }
    }

    public String getGeofenceTransitionDetails(GeofenceBroadcastReceiver receiver, int transition, List<Geofence> triggers ){
        return (receiver.toString() + " " + String.valueOf(transition));
    }
    public void sendNote( String Details, Context context){
        Intent intent = new Intent(context,  MainActivity.class);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

// build notification
// the addAction re-use the same intent to keep the example short
        NotificationCompat.Builder n  = new NotificationCompat.Builder(context)
                .setContentTitle("Don't Forget!")
                .setContentText("We noticed you are leaving and forgot to track your Synthroid!")
                .setSmallIcon(R.drawable.ic_menu_manage)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
//                        .addAction(R.drawable., "Call", pIntent)
                .addAction(R.drawable.ic_launcher_foreground, "Track", pIntent);
        n.setDefaults(Notification.DEFAULT_SOUND);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "ecse410";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            n.setChannelId(channelId);
        }

        Log.i(TAG, "in NOTIFY");
        notificationManager.notify(1, n.build());
    }
}