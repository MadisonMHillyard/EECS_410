package com.example.assignment3;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.Date;
import java.util.List;

@Dao
public interface TrackerDao {

    // allowing the insert of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Tracker tracker);

    @Query("DELETE FROM tracker_table")
    void deleteAll();

    @Query("SELECT * from tracker_table ORDER BY tracker ASC")
    LiveData<List<Tracker>> getAlphabetizedWords();

    @Query("UPDATE tracker_table SET numMissed = numMissed +1 WHERE tracker =:tracker")
    void updateMissed(String tracker);

    @Query("UPDATE tracker_table SET numTracked = numTracked +1 WHERE tracker =:tracker")
    void updateTracked(String tracker);

    @Query("UPDATE tracker_table SET reminderHour=:hour, reminderMinute=:min WHERE tracker =:tracker")
    void updateReminder(int hour, int min, String tracker);

    @Query("UPDATE tracker_table SET lat=:lat, lng=:lng WHERE tracker =:tracker")
    void updateLocation(double lat, double lng, String tracker);

    @Query("UPDATE tracker_table SET radius=:radius WHERE tracker =:tracker")
    void updateGeoFenceRadius(int radius, String tracker);

    @Query("UPDATE tracker_table SET dateLastTracked=:date WHERE tracker =:tracker")
    void updateLastTracked(String date, String tracker);
}