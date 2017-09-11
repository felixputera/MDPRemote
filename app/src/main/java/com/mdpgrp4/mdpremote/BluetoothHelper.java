package com.mdpgrp4.mdpremote;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by felix on 9/10/2017.
 */

public class BluetoothHelper {
    public static final int REQUEST_ENABLE_BT = 10;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothHelper() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public boolean bluetoothIsAvailable() {
        return bluetoothAdapter != null;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

}
