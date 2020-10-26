package com.example.assignment3.ui;

import com.google.android.gms.maps.model.LatLng;

public interface GeoFenceListener {
    void onUpdateCurrentLocation(LatLng latLng);
}
