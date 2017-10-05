package com.mdpgrp4.mdpremote;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class TiltSteeringActivity extends AppCompatActivity implements SensorEventListener {

    private final double MAX_TILT = 5;
    private final double FILTER_ALPHA = 0.05;
    BluetoothService mService;
    boolean mBound = false;
    private TextView hView, vView;
    private SensorManager sensorManager;
    private double xTilt = 0;
    private double verTilt = 0;
    private double baseVerTilt = 0;
    private boolean calibrate = false;
    private long lastUpdate = 0;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothService.BluetoothBinder binder = (BluetoothService.BluetoothBinder) iBinder;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilt_steering);
        hView = (TextView) findViewById(R.id.h_acc);
        vView = (TextView) findViewById(R.id.v_acc);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        displayDialog();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent bluetoothIntent = new Intent(this, BluetoothService.class);
        bindService(bluetoothIntent, mConnection, BIND_ABOVE_CLIENT);
    }

    @Override
    protected void onStop() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (calibrate) {
                verTilt = sensorEvent.values[1] - sensorEvent.values[2];
                baseVerTilt = verTilt;
                calibrate = false;
            }
            getAccelerometer(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void getAccelerometer(SensorEvent sensorEvent) {

        double newXTilt = sensorEvent.values[0];
        if (newXTilt > MAX_TILT) {
            newXTilt = MAX_TILT;
        } else if (newXTilt < -MAX_TILT) {
            newXTilt = -MAX_TILT;
        }

        xTilt = xTilt + (FILTER_ALPHA * (newXTilt - xTilt));

        double xDirection = xTilt;
        int multiplier = 1;
        if (xDirection < 0) {
            multiplier = -1;
            xDirection *= -1;
        }

        xDirection /= MAX_TILT;

        xDirection *= multiplier;
        hView.setText(String.valueOf((float) xDirection));

        double newVerTilt = sensorEvent.values[1] - sensorEvent.values[2];
        if (newVerTilt > baseVerTilt + MAX_TILT) {
            newVerTilt = baseVerTilt + MAX_TILT;
        } else if (newVerTilt < baseVerTilt - MAX_TILT) {
            newVerTilt = baseVerTilt - MAX_TILT;
        }

        verTilt = verTilt + (FILTER_ALPHA * (newVerTilt - verTilt));

        double verDirection = verTilt;

        verDirection = (verDirection - baseVerTilt) / MAX_TILT;
        vView.setText(String.valueOf((float) verDirection));

        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 500) {
            if (mBound) {
                if (xDirection < -0.6) {
                    mService.writeBtOut("tr");
                } else if (xDirection > 0.6) {
                    mService.writeBtOut("tl");
                }
                if (verDirection > 0.8) {
                    mService.writeBtOut("r");
                } else if (verDirection < -0.8) {
                    mService.writeBtOut("f");
                }
            }
            lastUpdate = curTime;
        }
    }

    private void displayDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TiltSteeringActivity.this);
        dialogBuilder.setMessage("Hold your phone in landscape. Press OK to set current sensor readings as baseline.");
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                calibrate = true;
                dialogInterface.dismiss();
            }
        });
        dialogBuilder.setCancelable(false);
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
