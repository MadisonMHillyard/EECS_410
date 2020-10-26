package com.example.assignment3.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.assignment3.MainActivity;
import com.example.assignment3.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    TextView setReminder;
    TextView setGeoFence;
    MainActivity activity;
    private Button track;
    private Button miss;
    public static final String EXTRA_REPLY = "com.example.android.wordlistsql.REPLY";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        activity = (MainActivity)getActivity();
        activity.homeFrag = this;
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        setReminder = (TextView)root.findViewById(R.id.homeSetReminder);
        setGeoFence = (TextView)root.findViewById(R.id.homeSetGeoFence);
        track = (Button) root.findViewById(R.id.track);
        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trackPill(view);
            }
        });
        miss = (Button) root.findViewById(R.id.miss);
        miss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                missPill(view);
            }
        });
        place_set();
        reminder_set();

        return root;
    }

    public void missPill(View view){
        activity.onUpdateMissed();

    }
    public void trackPill(View view){
        activity.onUpdateTracked();
    }

    public void place_set(){
        if (activity.place_set == false){
            setGeoFence.setText("Please set Location to take your pill!");
        }else{
            setGeoFence.setText("");
        }
    }

    public void reminder_set(){
        if (activity.reminder_set == false){
            setReminder.setText("Please set a calendar reminder!");
        }else{
            setReminder.setText("");
        }
    }
}