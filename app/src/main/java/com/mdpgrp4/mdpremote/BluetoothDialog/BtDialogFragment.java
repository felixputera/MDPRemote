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

import com.mdpgrp4.mdpremote.BluetoothService;
import com.mdpgrp4.mdpremote.R;

import java.util.Set;

/**
 * Created by felix on 9/10/2017.
 */

public class BtDialogFragment extends DialogFragment {
    private BluetoothAdapter btAdapter;
    private BluetoothService btService;
    private View mView;
    private final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            ProgressBar scanProgress = mView.findViewById(R.id.progressScanning);
            Button scanButton = mView.findViewById(R.id.buttonDialogScan);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                addDeviceToView(device);
//                Log.d("Device found: ", device.getName());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                LinearLayout deviceLayout = mView.findViewById(R.id.deviceLayout);
                deviceLayout.removeAllViews();
                initPairedDeviceView();
                scanProgress.setVisibility(View.VISIBLE);
                scanButton.setText(R.string.bt_dialog_stop_scan);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanProgress.setVisibility(View.GONE);
                scanButton.setText(R.string.bt_dialog_scan);
            } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                TextView scanMode = mView.findViewById(R.id.btScanMode);
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

    public static BtDialogFragment newInstance(BluetoothAdapter btAdapter, BluetoothService btService) {
        BtDialogFragment btDialog = new BtDialogFragment();
        btDialog.setBtAdapter(btAdapter);
        btDialog.setBtService(btService);
        return btDialog;
    }

    private void addDeviceToView(BluetoothDevice device) {
        LinearLayout deviceLayout = mView.findViewById(R.id.deviceLayout);
        DeviceView deviceView = new DeviceView(getActivity());
        deviceView.setDevice(device);
        deviceView.setBtService(btService);
        deviceLayout.addView(deviceView);
        deviceLayout.invalidate();
    }

    private void initPairedDeviceView() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                addDeviceToView(device);
            }
        }

    }

    private void setBtAdapter(BluetoothAdapter btAdapter) {
        this.btAdapter = btAdapter;
    }

    private void setBtService(BluetoothService btService) {
        this.btService = btService;
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

        btAdapter.startDiscovery();
    }

    @Override
    public void onResume() {


        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.dialog_bluetooth, container, false);
        final Button buttonDone = mView.findViewById(R.id.buttonDialogDone);
        buttonDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        final Button buttonScan = mView.findViewById(R.id.buttonDialogScan);
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

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 60);
        startActivity(discoverableIntent);

        initPairedDeviceView();

        return mView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        btAdapter.cancelDiscovery();
        getActivity().unregisterReceiver(btReceiver);
    }
}
