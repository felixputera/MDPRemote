package com.mdpgrp4.mdpremote;

/**
 * Created by felix on 9/30/2017.
 */

public class BluetoothEvent {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTED = 1;
    public static final int MESSAGE_RECEIVED = 2;

    public final int status;
    public final String message;

    public BluetoothEvent(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public BluetoothEvent(int status) {
        this.status = status;
        this.message = "";
    }
}
