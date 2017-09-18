package com.mdpgrp4.mdpremote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by felix on 9/17/2017.
 */

public class BtConnectionThread extends Thread {
    private BluetoothSocket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private byte[] mBuffer;
    private BluetoothAdapter btAdapter;
    private Activity mActivity;

    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageConstants.MESSAGE_IN:
                    String message = new String((byte[]) msg.obj);
                    Log.d("MDP_REMOTE_DEBUG", "Message: " + message);
                    break;
                case MessageConstants.MESSAGE_OUT:
                    break;
                case MessageConstants.BT_CONNECTED:
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) mActivity).getBtDialog().dismiss();
                        }
                    });
                    break;
                case MessageConstants.BT_DISCONNECTED:
                    break;
            }
        }
    };

    public BtConnectionThread(BluetoothAdapter btAdapter, Activity mActivity) {
        this.btAdapter = btAdapter;
        this.mActivity = mActivity;

//        mHandler.sendEmptyMessage(MessageConstants.BT_CONNECTED);
    }

    @Override
    public void run() {
        mBuffer = new byte[1024];
        int numBytes; // bytes returned from read()
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mInStream.read(mBuffer);
                // Send the obtained bytes to the UI activity.
                Message arriveMsg = mHandler.obtainMessage(MessageConstants.MESSAGE_IN,
                        0, numBytes, mBuffer);
                arriveMsg.sendToTarget();
            } catch (IOException e) {
                Log.d("MDP_REMOTE_DEBUG", "Input stream was disconnected", e);
                // create bluetooth server after disconnection
                BtServerThread serverThread = new BtServerThread(btAdapter, mActivity);
                serverThread.start();
                break;
            }
        }
    }

    public void setBtSocket(BluetoothSocket socket) {
        mSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            tmpIn = socket.getInputStream();
        } catch (IOException e) {
            Log.e("MDP_REMOTE_ERROR", "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e("MDP_REMOTE_ERROR", "Error occurred when creating output stream", e);
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
    }

    private interface MessageConstants {
        public static final int MESSAGE_IN = 0;
        public static final int MESSAGE_OUT = 1;
        public static final int BT_CONNECTED = 2;
        public static final int BT_DISCONNECTED = 3;
    }

}
