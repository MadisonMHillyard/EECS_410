package com.example.assignment3;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "tracker_table")
public class Tracker {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "tracker")
    private String mTracker;
    public Tracker(String tracker){ this.mTracker = tracker;}
    public String getTracker(){return this.mTracker;}

    @ColumnInfo(name = "numTracked")
    public int numTracked;

    @ColumnInfo(name = "numMissed")
    public int numMissed;

    @ColumnInfo(name = "radius")
    public int radius;

    @ColumnInfo(name = "lat")
    public double lat;

    @ColumnInfo(name = "lng")
    public double lng;

    @ColumnInfo(name = "reminderHour")
    public int hour;

    @ColumnInfo(name = "reminderMinute")
    public int min;

    @ColumnInfo(name = "dateLastTracked")
    public String dateLastTracked;

}
