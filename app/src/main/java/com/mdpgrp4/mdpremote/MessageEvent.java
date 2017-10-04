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
    public static final int ROBOT_ORIENTATION = 4;
    public static final int MAP = 5;
    public static final int CONNECT_DEVICE = 10;
    public static final int CONNECT_FAIL = 11;
    public static final int INVALID_JSON = 99;

    public final int status;
    public final String message;
    public final int[] coordinates;
    public final int robotOrientation;
    public final String[] map;
    public BluetoothDevice device;

    public MessageEvent(int status, String message) {
        this.status = status;
        this.message = message;
        this.coordinates = new int[2];
        this.robotOrientation = 0;
        this.map = new String[2];
    }

    public MessageEvent(int status) {
        this.status = status;
        this.message = "";
        this.coordinates = new int[2];
        this.robotOrientation = 0;
        this.map = new String[2];
    }

    public MessageEvent(int status, int robotOrientation) {
        this.status = status;
        this.message = "";
        this.coordinates = new int[2];
        this.robotOrientation = robotOrientation;
        this.map = new String[2];
    }

    public MessageEvent(int status, String[] map) {
        this.status = status;
        this.message = "";
        this.coordinates = new int[2];
        this.robotOrientation = 0;
        this.map = map;
    }

    public MessageEvent(int status, int[] coordinates) {
        this.status = status;
        this.message = "";
        this.coordinates = coordinates;
        this.robotOrientation = 0;
        this.map = new String[2];
    }

    public MessageEvent(int status, BluetoothDevice device) {
        this.status = status;
        this.device = device;
        this.message = "";
        this.coordinates = new int[2];
        this.robotOrientation = 0;
        this.map = new String[2];
    }
}
