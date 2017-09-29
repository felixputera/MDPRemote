package com.mdpgrp4.mdpremote;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

/**
 * Created by felix on 9/22/2017.
 */

public class BluetoothService extends Service {

    private MainActivityCallbacks callbacks;
    private final IBinder mBinder = new BluetoothBinder();

    public class BluetoothBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Activity activity = (Activity) intent.getSerializableExtra("activity");
        // create & start bluetooth server thread waiting for connection
        BtServerThread serverThread = new BtServerThread(BluetoothAdapter.getDefaultAdapter(), callbacks.getCurrentActivity());
        serverThread.start();

        return mBinder;
    }

}
