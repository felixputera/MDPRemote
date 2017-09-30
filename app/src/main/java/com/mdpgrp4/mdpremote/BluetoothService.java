package com.mdpgrp4.mdpremote;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



public class BluetoothService extends Service {

    private static final String NAME = "MDP_REMOTE";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private final IBinder mBinder = new BluetoothBinder();
    private final String TAG = "BLUETOOTH_SERVICE";
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    private BtConnectionThread mConnectionThread;
    private BtListenerThread mListenerThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Started service");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // create & start bluetooth server thread waiting for connection
        Log.d(TAG, "Bound to service");
        mListenerThread = new BtListenerThread();
        mListenerThread.start();

        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConnectionThread != null) {
            mConnectionThread.cancel();
        }
        if (mListenerThread != null) {
            mListenerThread.cancel();
        }
    }

    public void disconnectBtSocket() {
        if (mConnectionThread != null) {
            mConnectionThread.cancel();
            EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.DISCONNECTED));
        }
    }

    public class BluetoothBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    private class BtConnectionThread extends Thread {
        private BluetoothSocket mSocket;
        private InputStream mInStream;
        private OutputStream mOutStream;
        private byte[] mBuffer;

        @Override
        public void run() {
            HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
            handlerThread.start();

            Looper looper = handlerThread.getLooper();

            EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.CONNECTED));

            mBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mInStream.read(mBuffer);
                    String msg = new String(mBuffer);
                    Log.d(TAG, "Message: " + msg.substring(0, numBytes));

                    // Send the obtained bytes to the UI activity.
                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.MESSAGE_RECEIVED,
                            msg.substring(0, numBytes)));
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    EventBus.getDefault().post(new BluetoothEvent(BluetoothEvent.DISCONNECTED));
                    // create bluetooth server thread after disconnection
                    mListenerThread = new BtListenerThread();
                    mListenerThread.start();
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


        public void write(byte[] bytes) {
            try {
                mOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("MDP_REMOTE_ERROR", "Error occurred when sending data", e);

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


    }


    private class BtListenerThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public BtListenerThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = btAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;

        }

        @Override
        public void run() {
            Log.d(TAG, "Started ListenerThread");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    Log.d(TAG, "Bluetooth socket connected");
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
                    mConnectionThread = new BtConnectionThread();
                    mConnectionThread.setBtSocket(socket);
                    mConnectionThread.start();
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Could not close the connect socket", e);
                    }
                    break;
                }
            }

        }

        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }

        }
    }
}
