# EECS_410
Mobile Health

# Author
Madison M. Hillyard
madison.hillyard@case.edu

# Description
This application plots real time sampled data from the device's accelerometer and allows the user to export the data.

# Development Info

This Code only tested on a Samsung Galaxy S8 Phone and may not be cross compatible to different screen types. 

Please note the device that runs this application must be capable of accessing an internal Accelerometer sensor.

For best use export to Google Drive

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
│       │   │           └───assignment1
│       │   └───res
│       └───test

├───gradle
│   └───wrapper
├───old
└───spotify-app-remote-release-0.7.1

```

## Java Classes

### MainActivity.java

Controls the UI and instantiates the other functionality.

### MusicManager.java

Manages the Music Connection to Spotify

### SensorFilter.java

Filters the Accelorometer Sensor Data

### StepDetector.java

Main Step Detection Class. Contains a Step Listener and updates computed acceleration

### Steplistener.java

Listener Interface

# Compilation and Dependencies

All dependencies are noted in the app level gradle file.

Please note this application uses and depends on Spotify's Android SDK and Google Map's SDK