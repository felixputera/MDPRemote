package com.mdpgrp4.mdpremote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by felix on 9/14/2017.
 */

public class BtServerThread extends Thread {
    private static final String NAME = "MDP_REMOTE";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private final BluetoothServerSocket mmServerSocket;
    private BluetoothAdapter btAdapter;
    private Activity mActivity;

    public BtServerThread(BluetoothAdapter btAdapter, Activity mActivity) {
        BluetoothServerSocket tmp = null;
        try {
            tmp = btAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
        } catch (IOException e) {
            Log.e("MDP_REMOTE", "Socket's listen() method failed", e);
        }
        mmServerSocket = tmp;
        this.btAdapter = btAdapter;
        this.mActivity = mActivity;
    }

    @Override
    public void run() {

        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e("MDP_REMOTE", "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                Log.d("MDP_REMOTE", "Bluetooth socket connected");
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                BtConnectionThread connectionThread = new BtConnectionThread(btAdapter, mActivity);
                connectionThread.setBtSocket(socket);
                connectionThread.start();
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    Log.e("MDP_REMOTE", "Could not close the connect socket", e);
                }
                break;
            }
        }

    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            Log.e("BtThreadError", "Could not close the connect socket", e);
        }

    }
}
