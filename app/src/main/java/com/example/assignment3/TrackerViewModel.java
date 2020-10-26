package com.example.assignment3;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

public class TrackerViewModel extends AndroidViewModel {

    private TrackerRepository mRepository;

    private LiveData<List<Tracker>> mAllTrackers;

    public TrackerViewModel (Application application) {
        super(application);
        mRepository = new TrackerRepository(application);
        mAllTrackers = mRepository.getAllTrackers();
    }

    LiveData<List<Tracker>> getAllTrackers() { return mAllTrackers; }

    public void insert(Tracker tracker) { mRepository.insert(tracker); }

    public void deleteAllTrackers() { mRepository.deleteAllTrackers(); }

    public void updateMissed(Tracker tracker) { mRepository.updateMissed(tracker); }
    public void updateTracked(Tracker tracker) { mRepository.updateTracked(tracker); }
    public void updateLocation(double lat, double lng, Tracker tracker) { mRepository.updateLocation(lat, lng, tracker); }
    public void updateReminder(int hour, int min, Tracker tracker) { mRepository.updateReminder(hour, min, tracker); }
    public void updateGeoFenceRadius(int radius, Tracker tracker) {mRepository.updateGeoFenceRadius(radius, tracker);}
    public void updateLastTracked(String dateLastTracked, Tracker tracker) {mRepository.updateLastTracked(dateLastTracked, tracker);}
}
