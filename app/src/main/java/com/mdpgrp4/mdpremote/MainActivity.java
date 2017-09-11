package com.mdpgrp4.mdpremote;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.mdpgrp4.mdpremote.BluetoothDialog.BtDialogFragment;


public class MainActivity extends AppCompatActivity {

    private MapView mapView;
    private BluetoothHelper bluetoothHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);

        int[][] tileStatus = new int[15][20];
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 20; y++) {
                tileStatus[x][y] = MapView.STATUS_ROBOT;
            }
        }
        mapView.setTileStatus(tileStatus);

        bluetoothHelper = new BluetoothHelper();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_tilt_steering:
                Intent tilt_steering_intent = new Intent(this, TiltSteeringActivity.class);
                startActivity(tilt_steering_intent);
                return true;
            case R.id.action_bluetooth_connect:
                enableBluetooth();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void enableBluetooth() {
        if (!bluetoothHelper.getBluetoothAdapter().isEnabled()
                && bluetoothHelper.bluetoothIsAvailable()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothHelper.REQUEST_ENABLE_BT);
        } else {
            openBtDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                openBtDialog();
            }
        }
    }

    private void openBtDialog() {
        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("btDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment btDialog = BtDialogFragment.newInstance(bluetoothHelper.getBluetoothAdapter());
        btDialog.show(ft, "btDialog");
    }
}
