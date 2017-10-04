package com.mdpgrp4.mdpremote.BluetoothDialog;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mdpgrp4.mdpremote.BluetoothService;
import com.mdpgrp4.mdpremote.MessageEvent;
import com.mdpgrp4.mdpremote.R;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by felix on 9/12/2017.
 */

class DeviceView extends LinearLayout {
    private GestureDetectorCompat detector;
    private BluetoothDevice device;
    private BluetoothService btService;

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

    public void setDevice(BluetoothDevice device) {
        this.device = device;
        TextView deviceNameView = findViewById(R.id.deviceName);
        TextView deviceMacView = findViewById(R.id.deviceMac);
        deviceNameView.setText(device.getName());
        deviceMacView.setText(device.getAddress());
    }

    public void setBtService(BluetoothService btService) {
        this.btService = btService;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        detector.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    private class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.d("MAC: ", device.getAddress());
            EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECT_DEVICE, device));
            return true;
        }
    }
}
