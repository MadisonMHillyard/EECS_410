package com.example.assignment3.ui.geofence;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GeofenceViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GeofenceViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Click the map to set the location you usually take your pill and adjust the GeoFence");
    }

    public LiveData<String> getText() {
        return mText;
    }
}