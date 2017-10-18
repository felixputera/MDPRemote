package com.mdpgrp4.mdpremote;

import android.bluetooth.BluetoothDevice;

/**
 * Created by felix on 9/30/2017.
 */

public class MessageEvent {

    public static final int DISCONNECTED = 0;
    public static final int CONNECTED = 1;
    public static final int ROBOT_STATUS = 2;
    public static final int ROBOT_POS = 3;
    public static final int MAP = 5;
    public static final int CONNECT_DEVICE = 10;
    public static final int CONNECT_FAIL = 11;
    public static final int INVALID_JSON = 99;

    public final int status;
    public final String message;
    public final String[] coordinates;
    public final int robotOrientation;
    public final String[] map;
    public final BluetoothDevice device;

    public MessageEvent(int status, String message) {
        this.status = status;
        this.message = message;
        this.coordinates = new String[2];
        this.robotOrientation = 0;
        this.map = new String[2];
        this.device = null;
    }

    public MessageEvent(int status) {
        this.status = status;
        this.message = "";
        this.coordinates = new String[2];
        this.robotOrientation = 0;
        this.map = new String[2];
        this.device = null;
    }

    public MessageEvent(int status, String[] map, String[] coordinates, BluetoothDevice device) {
        this.status = status;
        this.message = "";
        this.coordinates = coordinates;
        this.robotOrientation = 0;
        this.map = map;
        this.device = device;
    }
}
