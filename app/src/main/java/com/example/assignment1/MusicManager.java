package com.example.assignment1;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;



public class MusicManager {
    private static final String CLIENT_ID = "fa23981f16ec40788e3b2f02d1e5feb8";
    private static final String REDIRECT_URI = "http://com.example.assignment1/callback";
    private SpotifyAppRemote mSpotifyAppRemote;
    ConnectionParams connectionParams;


    public enum MusicState{
        NOT_STARTED,
        PLAYING,
        PAUSED
    }

    public MusicState mState;
    public boolean tracking = false;
    public String defaultPlaylist = "spotify:playlist:0whX9iLOo6Bf46EBdPaT6b";
    public String activityPlaylist = "";
    public String nextPlaylist = "";
    public void startMusicManager (Context context){
        // Set the connection parameters
        mState = MusicState.NOT_STARTED;
        connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(context, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
//                        spotifyConnected();

                    }


                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }


    public void stopMusicManager() {
        mState = MusicState.NOT_STARTED;
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void spotifyConnected() {
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });

    }

    public void start(){
        if (mSpotifyAppRemote != null){
            if (mState == MusicState.NOT_STARTED ||mState == MusicState.PAUSED){
                if (nextPlaylist != ""){
                    mSpotifyAppRemote.getPlayerApi().play(activityPlaylist);
                    nextPlaylist = "";
                }
                else if (activityPlaylist != ""){
                    mSpotifyAppRemote.getPlayerApi().play(activityPlaylist);
                }
                else{
                    mSpotifyAppRemote.getPlayerApi().play(defaultPlaylist);
                }
            }
            else {
                mSpotifyAppRemote.getPlayerApi().resume();
            }
            mState = MusicState.PLAYING;
        } else{
        }
    }

    public void toggletracking(boolean state){
        if (state == true){
            tracking = true;

        }
        else{
            tracking = false;
            Log.i("Music", "Music State: " + String.valueOf(mState));
            changePlaylist(defaultPlaylist);
        }
    }

    public void changePlaylist(String playlist){
        if (mSpotifyAppRemote != null) {
            Log.i("Music", "Music State: " + String.valueOf(mState));
            if (mState == MusicState.NOT_STARTED || mState == MusicState.PAUSED) {
                Log.i("Music", "In not started or paused");
                nextPlaylist = playlist;
            } else {
                mSpotifyAppRemote.getPlayerApi().play(playlist);
                mState = MusicState.PLAYING;
            }
        }
    }
    public void changeActivityState(String playlist){
        activityPlaylist = playlist;
        changePlaylist(playlist);
    }
    public void pause(){
        if (mSpotifyAppRemote != null){
            mSpotifyAppRemote.getPlayerApi().pause();
            mState = MusicState.PAUSED;
        }
    }

    public void next(){
        if (mSpotifyAppRemote != null){
            mSpotifyAppRemote.getPlayerApi().skipNext();
        }
    }

    public void previous(){
        if (mSpotifyAppRemote != null){
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }

}
