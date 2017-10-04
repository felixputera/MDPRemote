package com.mdpgrp4.mdpremote;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
            EventBus.getDefault().post(new MessageEvent(MessageEvent.DISCONNECTED));
        }
    }

    public void writeBtOut(String data) {
        if (mConnectionThread != null) {
            mConnectionThread.write(data.getBytes());
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

            EventBus.getDefault().post(new MessageEvent(MessageEvent.CONNECTED));

            mBuffer = new byte[1024];
            int numBytes; // bytes returned from read()
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mInStream.read(mBuffer);
                    String msg = new String(mBuffer);
                    String jsonMessage = msg.substring(0, numBytes);
                    Log.d(TAG, "Message: " + jsonMessage);

                    try {
                        // Send the obtained bytes to the UI activity.
                        Gson gson = new Gson();
                        IncomingMessage message = gson.fromJson(jsonMessage, IncomingMessage.class);

                        if (message.robotStatus != null) {
                            EventBus.getDefault().post(new MessageEvent(MessageEvent.ROBOT_STATUS,
                                    message.robotStatus));
                        }
                        if (message.robotPosition != null) {
                            EventBus.getDefault().post(new MessageEvent(MessageEvent.ROBOT_POS,
                                    message.robotPosition));
                        }
                        if (message.robotOrientation != null) {
                            EventBus.getDefault().post(new MessageEvent(MessageEvent.ROBOT_ORIENTATION,
                                    message.robotOrientation));
                        }
                        if (message.mapObstacle != null) {
                            EventBus.getDefault().post(new MessageEvent(MessageEvent.MAP,
                                    new String[]{message.mapObstacle, message.mapExplored}));
                        }
                    } catch (JsonSyntaxException e) {
                        EventBus.getDefault().post(new MessageEvent(MessageEvent.INVALID_JSON));
                    }
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    EventBus.getDefault().post(new MessageEvent(MessageEvent.DISCONNECTED));
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
