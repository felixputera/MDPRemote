package com.mdpgrp4.mdpremote.BluetoothDialog;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mdpgrp4.mdpremote.R;

/**
 * Created by felix on 9/12/2017.
 */

class DeviceView extends LinearLayout {
    private String deviceAddress;
    private String deviceName;
    private GestureDetectorCompat detector;

    public DeviceView(Context context) {
        super(context);
        init(context);
    }

    public DeviceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DeviceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.view_bluetooth_device, this);

        detector = new GestureDetectorCompat(getContext(), new GestureTap());
    }

    public void setDeviceValues(String dAddress, String dName) {
        deviceAddress = dAddress;
        deviceName = dName;
        TextView deviceTV = (TextView) findViewById(R.id.deviceTV);
        deviceTV.setText(deviceName);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            Log.d("MAC: ", deviceAddress);
            return true;
        }
    }
}
