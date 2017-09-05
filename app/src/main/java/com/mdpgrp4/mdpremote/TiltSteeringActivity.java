package com.mdpgrp4.mdpremote;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class TiltSteeringActivity extends AppCompatActivity implements SensorEventListener {

    private TextView xView, yView, zView;
    private SensorManager sensorManager;
    private float[] calibratedAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilt_steering);
        xView = (TextView) findViewById(R.id.x_acc);
        yView = (TextView) findViewById(R.id.y_acc);
        zView = (TextView) findViewById(R.id.z_acc);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        displayDialog();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void getAccelerometer(SensorEvent sensorEvent) {
        float[] accValues = sensorEvent.values;
        float x = accValues[0];
        float y = accValues[1];
        float z = accValues[2];

        xView.setText(String.valueOf(x));
        yView.setText(String.valueOf(y));
        zView.setText(String.valueOf(z));
    }

    private void displayDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TiltSteeringActivity.this);
        dialogBuilder.setMessage("Hold your phone in landscape. Press OK to set current sensor readings as baseline.");
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
