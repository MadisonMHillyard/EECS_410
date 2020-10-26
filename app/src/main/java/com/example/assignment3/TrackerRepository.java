package com.example.assignment3;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

class TrackerRepository {

    private TrackerDao mTrackerDao;
    private LiveData<List<Tracker>> mAllTrackers;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    TrackerRepository(Application application) {
        TrackerRoomDatabase db = TrackerRoomDatabase.getDatabase(application);
        mTrackerDao = db.TrackerDao();
        mAllTrackers = mTrackerDao.getAlphabetizedWords();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Tracker>> getAllTrackers() {
        return mAllTrackers;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Tracker tracker) {
        TrackerRoomDatabase.databaseWriteExecutor.execute(() -> {
            mTrackerDao.insert(tracker);
        });
    }
    void deleteAllTrackers() {
        TrackerRoomDatabase.databaseWriteExecutor.execute(() -> {
            mTrackerDao.deleteAll();
        });
    }

    void updateMissed(Tracker tracker){
        TrackerRoomDatabase.databaseWriteExecutor.execute(() -> {
            mTrackerDao.updateMissed(tracker.getTracker());
        });
    }

    void updateTracked(Tracker tracker){
        TrackerRoomDatabase.databaseWriteExecutor.execute(() -> {
            mTrackerDao.updateTracked(tracker.getTracker());
        });
    }
    void updateReminder(int hour, int min, Tracker tracker){
        TrackerRoomDatabase.databaseWriteExecutor.execute(() -> {
            mTrackerDao.updateReminder(hour, min, tracker.getTracker());
        });
    }

    void updateGeoFenceRadius(int radius, Tracker tracker){
        TrackerRoomDatabase.databaseWriteExecutor.execute(() -> {
            mTrackerDao.updateGeoFenceRadius(radius, tracker.getTracker());
        });
    }

    void updateLocation(double lat, double lng,Tracker tracker){
        TrackerRoomDatabase.databaseWriteExecutor.execute(() -> {
            mTrackerDao.updateLocation(lat, lng, tracker.getTracker());
        });
    }
    void updateLastTracked(String dateLastTracked, Tracker tracker) {
        TrackerRoomDatabase.databaseWriteExecutor.execute(() -> {
            mTrackerDao.updateLastTracked(dateLastTracked, tracker.getTracker());
        });
    }


    }