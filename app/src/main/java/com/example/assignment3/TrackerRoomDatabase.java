package com.example.assignment3;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Tracker.class}, version = 1, exportSchema = false)
public abstract class TrackerRoomDatabase extends RoomDatabase {

    public abstract TrackerDao TrackerDao();

    private static volatile TrackerRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
//            databaseWriteExecutor.execute(() -> {
//                // Populate the database in the background.
//                // If you want to start with more words, just add them.
//                TrackerDao dao = INSTANCE.TrackerDao();
//                dao.deleteAll();
//
//                Tracker tracker = new Tracker("Hashimoto's Tracker");
//                tracker.numMissed = 0;
//                tracker.numTracked = 1;
//                tracker.lat = 0.0;
//                tracker.lng = 0.0;
//                tracker.radius = 0;
//                tracker.hour = 0;
//                tracker.min = 0;
//                dao.insert(tracker);
//            });
        }
    };
    static TrackerRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TrackerRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TrackerRoomDatabase.class,  "tracker_database.db")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}