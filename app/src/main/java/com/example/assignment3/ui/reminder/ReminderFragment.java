package com.example.assignment3.ui.reminder;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.assignment3.MainActivity;
import com.example.assignment3.R;

import java.util.Calendar;

public class ReminderFragment extends Fragment {
    private ReminderViewModel galleryViewModel;
    Button setReminder;
    Button setTime;
    private static String time;
    private static int hour = 7;
    private static int minute = 0;
    private static boolean is_AM = true;
    private static TextView timeView;
    private static final String TAG = ReminderFragment.class.getSimpleName();

    private MainActivity activity;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        galleryViewModel =
                ViewModelProviders.of(this).get(ReminderViewModel.class);

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        activity = (MainActivity) getActivity();
        activity.calFrag = this;
        final TextView textView = root.findViewById(R.id.text_gallery);
        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        setReminder = (Button) root.findViewById(R.id.setReminder);
        setReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar beginTime = Calendar.getInstance();
                int beginhour = hour;
                int endhour = hour+1;
                if (!is_AM){
                    beginhour = beginhour + 12;
                    if(beginhour == 23){
                        endhour = 0;
                    }else{
                        endhour = endhour +12;
                    }
                }
                activity.onUpdateReminder(beginhour, minute);
                beginTime.set( beginTime.get(Calendar.YEAR), beginTime.get(Calendar.MONTH), beginTime.get(Calendar.DATE), beginhour, minute);
                Calendar endTime = Calendar.getInstance();
                endTime.set( beginTime.get(Calendar.YEAR), beginTime.get(Calendar.MONTH), beginTime.get(Calendar.DATE), endhour, minute);
                Intent intent = new Intent(Intent.ACTION_INSERT)
                        .setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                        .putExtra(CalendarContract.Events.TITLE, "Reminder to Take Synthroid Pill")
                        .putExtra(CalendarContract.Events.DESCRIPTION, "Hashimoto's Reminder")
//                        .putExtra(CalendarContract.Events.DURATION, "Hashimoto's Reminder")
                        .putExtra(CalendarContract.Events.RRULE, "FREQ=DAILY;COUNT=10" ) // Recurrence rule
                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);

                startActivity(intent);

                activity.toggleReminder();
            }
        });

        setTime = (Button) root.findViewById(R.id.setTime);
        setTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog(view);
            }
        });

        timeView = (TextView) root.findViewById(R.id.editTextTime);
        setTimeString(hour, minute, is_AM);
        return root;
    }
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int min) {
            Log.i(TAG, "hour: "+ hourOfDay + ", minute: " + min);
            // if after noon 1-11:59
            if(hourOfDay > 11){
                hour = hourOfDay-12;
                is_AM = false;
            }else{
                if (hourOfDay == 00){
                    hour = 12;
                }else{
                    hour = hourOfDay;
                }
                is_AM = true;
            }
//            if (hourOfDay < 12 || hourOfDay == 24){
//                is_AM = true;
//            }
//            else{
//                is_AM = false;
//            }
            minute = min;

            setTimeString(hour, minute, is_AM);


        }
    }

    public void showTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
    }

    private static void setTimeString(int hour, int minute, boolean is_AM){

        time = String.valueOf(hour) + ":";

        if (minute < 10){
            time = time + "0" + String.valueOf(minute);
        }
        else{
            time = time + String.valueOf(minute);
        }
        if(is_AM){
            time = time + " AM";
        }
        else{
            time = time + " PM";
        }
        timeView.setText(time);
    }

}