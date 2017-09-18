package com.mdpgrp4.mdpremote.BluetoothDialog;

import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mdpgrp4.mdpremote.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by felix on 9/10/2017.
 */

public class BtDialogFragment extends DialogFragment {
    private Map<String, String> deviceMap = new HashMap<>();
    private BluetoothAdapter btAdapter;
    private View mView;
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            ProgressBar scanProgress = (ProgressBar) mView.findViewById(R.id.progressScanning);
            LinearLayout deviceLayout = mView.findViewById(R.id.deviceLayout);
            Button scanButton = (Button) mView.findViewById(R.id.buttonDialogScan);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                DeviceView deviceView = new DeviceView(context);
                deviceView.setDeviceValues(device.getAddress(), device.getName());
                deviceLayout.addView(deviceView);
                deviceLayout.invalidate();
                deviceMap.put(device.getAddress(), device.getName());
//                Log.d("Device found: ", device.getName());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                deviceLayout.removeAllViews();
                scanProgress.setVisibility(View.VISIBLE);
                scanButton.setText(R.string.bt_dialog_stop_scan);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanProgress.setVisibility(View.GONE);
                scanButton.setText(R.string.bt_dialog_scan);
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                TextView scanMode = (TextView) mView.findViewById(R.id.btScanMode);
                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        scanMode.setText(getResources().getString(R.string.bt_discoverable));
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        scanMode.setText(getResources().getString(R.string.bt_connectable));
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        scanMode.setText(getResources().getString(R.string.bt_none));
                        break;
                }
            }
        }
    };

    public static BtDialogFragment newInstance(BluetoothAdapter btAdapter) {
        BtDialogFragment btDialog = new BtDialogFragment();
        btDialog.setBtAdapter(btAdapter);
        return btDialog;
    }

    private void setBtAdapter(BluetoothAdapter btAdapter) {
        this.btAdapter = btAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(btReceiver, filter);

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
        startActivity(discoverableIntent);

        btAdapter.startDiscovery();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.dialog_bluetooth, container, false);
        final Button buttonDone = (Button) mView.findViewById(R.id.buttonDialogDone);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        final Button buttonScan = (Button) mView.findViewById(R.id.buttonDialogScan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scanString = getResources().getString(R.string.bt_dialog_scan);
                String stopString = getResources().getString(R.string.bt_dialog_stop_scan);
                if (buttonScan.getText() == scanString) {
                    btAdapter.startDiscovery();
                } else if (buttonScan.getText() == stopString) {
                    btAdapter.cancelDiscovery();
                }
            }
        });

        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        btAdapter.cancelDiscovery();
        getActivity().unregisterReceiver(btReceiver);
    }
}
