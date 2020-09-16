package com.example.assignment1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *MainActivity
 */
public class MainActivity extends AppCompatActivity {
    SensorManager sm = null;
    TextView currentData = null;
    List<Sensor> list;
    LineChart chart;
    ExtendedFloatingActionButton eFABToggle ;

    /**
     * Sensor Listener Instance
     */
    SensorEventListener sel = new SensorEventListener(){

        /**
         * Callback to handle changes in sensor accuracy
         * @param sensor
         * @param accuracy
         */
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        /**
         *Callback to hancle changes in sensor data
         * @param event
         */
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            currentData.setText("\tx: "+values[0]+"\n\ty: "+values[1]+"\n\tz: "+values[2]);
            addEntry(values[0], values[1], values[2]);
        }
    };


    /**
     * Activity class onCreate Override of class function
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Get Data Sampling Toggle Extended Floating Action Button */
        eFABToggle = (ExtendedFloatingActionButton)findViewById(R.id.toggleData);
        eFABToggle.setText("Pause");

        /* Get Reset Extended Floating Action Button */
        ExtendedFloatingActionButton eFABReset = (ExtendedFloatingActionButton)findViewById((R.id.resetData));
        eFABReset.setText("Reset");

        /* Get a SensorManager instance */
        sm = (SensorManager)getSystemService(SENSOR_SERVICE);

        /* Get Current Data  */
        currentData = (TextView)findViewById(R.id.current_data);

        /* Get List of Sensors */
        list = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);

        /* Register Sensor Listener */
        if(list.size()>0){
            sm.registerListener(sel, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
         /* Handle Lack of Sensor */
        }else{
            Toast.makeText(getBaseContext(), "Error: No Accelerometer.", Toast.LENGTH_LONG).show();
        }

        /* Initialize Chart */
        chart = (LineChart) findViewById(R.id.chart);
        initChart(chart);
    }

    /**
     * Activity class onResume Override of class function
     */
    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(sel, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Activity class onPause Override of class function
     */
    @Override
    protected void onPause() {
        String currText = (String)eFABToggle .getText();
        super.onPause();
        if(list.size() > 0 && currText.equals("Pause")){
            eFABToggle.setText("Start");
            sm.unregisterListener(sel);
        }
    }

    /**
     * Activity class onStop Override of class function
     */
    @Override
    protected void onStop() {
        String currText = (String)eFABToggle .getText();
        if(list.size()>0 && currText.equals("Pause")){
            eFABToggle.setText("Start");
            sm.unregisterListener(sel);
        }
        super.onStop();
    }


    /**
     * Add Individual Entry Data to chart
     * @param x
     * @param y
     * @param z
     */
    private void addEntry(float x, float y, float z){
        /* Get LineData for chart */
        LineData lineData = chart.getData();

        /* Get Data Sets */
        ILineDataSet setX = lineData.getDataSetByIndex(0);
        ILineDataSet setY = lineData.getDataSetByIndex(1);
        ILineDataSet setZ = lineData.getDataSetByIndex(2);

        /* Create Data Sets if needed */
        if (setX == null){
            setX = new LineDataSet(null, getResources().getString(R.string.data_label_x));
            initSet((LineDataSet)setX, Color.GREEN);
            lineData.addDataSet(setX);

        }
        if (setY == null){
            setY = new LineDataSet(null, getResources().getString(R.string.data_label_y));
            initSet((LineDataSet)setY, Color.BLUE);
            lineData.addDataSet(setY);
        }
        if (setZ == null){
            setZ = new LineDataSet(null, getResources().getString(R.string.data_label_z));
            initSet((LineDataSet)setZ, Color.RED);
            lineData.addDataSet(setZ);
        }

        /* Add Entries to data sets */
        lineData.addEntry(new Entry(setX.getEntryCount(), x), 0);
        lineData.addEntry(new Entry(setY.getEntryCount(), y), 1);
        lineData.addEntry(new Entry(setZ.getEntryCount(), z), 2);

        /* Add LineData and refresh chart */
        chart.setData(lineData);
        chart.invalidate();
    }

    /**
     *
     * @param chart
     */
    private void initChart(LineChart chart){

        chart.getDescription().setEnabled(true);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(true);

        chart.setBackgroundColor(Color.DKGRAY);
        chart.setDrawGridBackground(false);

        chart.setPinchZoom(true);

        LineData lineData = new LineData();
        lineData.setValueTextColor(Color.WHITE);
        chart.setData(lineData);

        chart.getDescription().setText(getResources().getString(R.string.axis_time));
        chart.getDescription().setTextColor(Color.WHITE);

        XAxis x = chart.getXAxis();
        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(false);
        x.setAvoidFirstLastClipping(true);
        x.setEnabled(true);
        x.setCenterAxisLabels(true);
        x.setDrawAxisLine(true);

        YAxis yleft = chart.getAxisLeft();
        yleft.setTextColor(Color.WHITE);
        yleft.setDrawGridLines(true);
        yleft.setDrawTopYLabelEntry(true);
        yleft.setEnabled(true);

        YAxis yright = chart.getAxisRight();
        yright.setEnabled(true);

        Legend legend = chart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.WHITE);
    }

    private LineDataSet initSet(LineDataSet set, int color) {
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setDrawCircles(false);
        set.setLineWidth(2f);
        set.setFillAlpha(65);
        set.setFillColor(color);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    /**
     * Toggle Data Gathering
     * @param view
     */
    public void toggleData(View view){
        String currText = (String)eFABToggle .getText();
        if (currText.equals("Pause")){
            eFABToggle.setText("Start");
            sm.unregisterListener(sel);
        }
        else{
            eFABToggle .setText("Pause");
            sm.registerListener(sel, (Sensor) list.get(0), SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Reset Chart Data
     * @param view
     */
    public void resetData(View view){
        /* Create new Line Data Object */
        LineData lineData = new LineData();
        lineData.setValueTextColor(Color.WHITE);

        /* Set New Line Data as chart Data and notify chart of refresh*/
        chart.setData(lineData);
        chart.notifyDataSetChanged();
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void export(View view){

        /* Stop Data Collection */
        String currText = (String)eFABToggle .getText();
        if (currText.equals("Pause")){
            eFABToggle.setText("Start");
            sm.unregisterListener(sel);
        }

        /* Get Line Data from chart */
        LineData lineData = chart.getData();

        /* Get Data Sets */
        ILineDataSet setX = lineData.getDataSetByIndex(0);
        ILineDataSet setY = lineData.getDataSetByIndex(1);
        ILineDataSet setZ = lineData.getDataSetByIndex(2);

        //generate data
        StringBuilder data = new StringBuilder();
        data.append("Index,x,y,z");
        for(int i = 0; i<setX.getEntryCount(); i++){
            data.append("\n" + String.valueOf(i) + ","
                    + String.valueOf(setX.getEntryForIndex(i).getY()) + ","
                    + String.valueOf(setY.getEntryForIndex(i).getY()) + ","
                    + String.valueOf(setZ.getEntryForIndex(i).getY()) + ",");
        }

        try{
            //saving the file into device
            FileOutputStream out = openFileOutput("data.csv", Context.MODE_PRIVATE);
            out.write((data.toString()).getBytes());
            out.close();

            //exporting
            Context context = getApplicationContext();
            File filelocation = new File(getFilesDir(), "data.csv");
            Uri path = FileProvider.getUriForFile(context, "com.example.exportcsv.fileprovider", filelocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv");
            LocalDateTime now = LocalDateTime.now();
            fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Sensor_"+now);
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            startActivity(Intent.createChooser(fileIntent, "Export Sensor Data"));
        }
        catch(Exception e){
            e.printStackTrace();
        }


    }

}
