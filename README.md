# EECS_410
Mobile Health

# Author
Madison M. Hillyard
madison.hillyard@case.edu

# Description
This application tracks Medication consistency for people with Hashimoto's Disease and hypothyroidism

# Development Info

This Code is only tested on a Samsung Galaxy S8 Phone and may not be cross compatible to different screen types. 

Please note the device that runs this application must be capable of Location Based, Android Calendar and Notification Services.

For best use please use Google Calendar

# Hierarchy

```console
├───.gradle  
├───.idea  
├───app
│   ├───build
│   ├───libs
│   └───src
│       ├───androidTest
│       ├───main
│       │   ├───java
│       │   │   └───com
│       │   │       └───example
│       │   │           └───assignment3
│       │   └───res
│       └───test

├───gradle
│   └───wrapper
├───old

```

## Java Classes

### MainActivity.java

- Controls the UI and instantiates the other functionality.

### GeofenceBroadcastReceiver.java

- BroadcastReceiver for the GeoFence transition Updates

### ui

#### GeofenceViewModel.java

- ViewModel for the GeofenceFragment

#### GeofenceFragment.java

- Fragment that handles all GeoFence Page actions

#### HomeViewModel.java

- ViewModel for the HomeFragment

#### HomeFragment.java

- Fragment that handles all Home Page actions

#### ReminderViewModel.java

- ViewModel for the ReminderFragment

#### ReminderFragment.java

- Fragment that handles all Reminder Page actions

#### GeoFenceListener.java

- Interface to enable Location updates in Fragment

### GeofenceBroadcastReceiver.java

- BroadcastReceiver for the GeoFence transition Updates

### Room database

### Tracker.java
- Tracker Entity

### TrackerDoa.java
- Tracker Data Access Object 
  
### TrackerRepository.java
- Tracker Repository

### TrackerRoomDatabase.java
- Tracker Database

### TrackerViewModel.java
- Tracker View Model
  
# Compilation and Dependencies

All dependencies are noted in the app level gradle file.
