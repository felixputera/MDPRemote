package com.mdpgrp4.mdpremote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by felix on 9/17/2017.
 */

public class BtConnectionThread extends Thread implements Handler.Callback {
    private final Object mutex = new Object();
    private BluetoothSocket mSocket;
    private InputStream mInStream;
    private OutputStream mOutStream;
    private byte[] mBuffer;
    private BluetoothAdapter btAdapter;
    private Activity mActivity;
    private Handler handler;
    private boolean running = true;

    public BtConnectionThread(BluetoothAdapter btAdapter, Activity mActivity) {
        this.btAdapter = btAdapter;
        this.mActivity = mActivity;
    }

    @Override
    public void run() {
        HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
        handlerThread.start();

        Looper looper = handlerThread.getLooper();

        handler = new Handler(looper, this);
        handler.sendEmptyMessage(MessageConstants.BT_CONNECTED);

        mBuffer = new byte[1024];
        int numBytes; // bytes returned from read()
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mInStream.read(mBuffer);
                // Send the obtained bytes to the UI activity.
                Message arriveMsg = handler.obtainMessage(MessageConstants.MESSAGE_IN,
                        0, numBytes, mBuffer);
                arriveMsg.sendToTarget();
            } catch (IOException e) {
                Log.d("MDP_REMOTE_DEBUG", "Input stream was disconnected", e);
                handler.sendEmptyMessage(MessageConstants.BT_DISCONNECTED);
                // create bluetooth server thread after disconnection
                BtServerThread serverThread = new BtServerThread(btAdapter, mActivity);
                serverThread.start();
                break;
            }

//            synchronized (mutex) {
//                try {
//                    mutex.wait();
//                } catch (InterruptedException e) {
//
//                }
//            }
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

//    public void shutdown() {
//        running = false;
//
//        synchronized (mutex) {
//            mutex.notifyAll();
//        }
//    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MessageConstants.MESSAGE_IN:
                String msg = new String((byte[]) message.obj);
                Log.d("MDP_REMOTE_DEBUG", "Message: " + msg);
                break;
            case MessageConstants.MESSAGE_OUT:
                break;
            case MessageConstants.BT_CONNECTED:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity) mActivity).getBtDialog().dismiss();
                        Toast.makeText(mActivity,
                                "Connected to " + mSocket.getRemoteDevice().getName(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case MessageConstants.BT_DISCONNECTED:
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity,
                                "Bluetooth disconnected",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            default:
                return false;
        }
        return true;
    }

    public void write(byte[] bytes) {
        try {
            mOutStream.write(bytes);

            // Share the sent message with the UI activity.
            Message writtenMsg = handler.obtainMessage(
                    MessageConstants.MESSAGE_OUT, -1, -1, mBuffer);
            writtenMsg.sendToTarget();
        } catch (IOException e) {
            Log.e("MDP_REMOTE_ERROR", "Error occurred when sending data", e);

            // Send a failure message back to the activity.
//            Message writeErrorMsg =
//                    handler.obtainMessage(MessageConstants.MESSAGE_TOAST);
//            Bundle bundle = new Bundle();
//            bundle.putString("toast",
//                    "Couldn't send data to the other device");
//            writeErrorMsg.setData(bundle);
//            handler.sendMessage(writeErrorMsg);
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e("MDP_REMOTE_ERROR", "Could not close the connect socket", e);
        }
    }

    private interface MessageConstants {
        int MESSAGE_IN = 0;
        int MESSAGE_OUT = 1;
        int BT_CONNECTED = 2;
        int BT_DISCONNECTED = 3;
    }

}
