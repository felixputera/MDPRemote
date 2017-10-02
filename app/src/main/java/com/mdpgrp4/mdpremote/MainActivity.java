package com.mdpgrp4.mdpremote;

import android.Manifest;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.mdpgrp4.mdpremote.BluetoothDialog.BtDialogFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MAIN_ACTIVITY";
    BluetoothService mService;
    boolean mBound = false;
    Intent bluetoothIntent = null;
    private MapView mapView;
    private BluetoothHelper bluetoothHelper;
    private DialogFragment btDialog;
    private boolean bluetoothIsStarted = false;
    private boolean bluetoothConnected = false;
    private ImageButton buttonRight, buttonLeft;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothService.BluetoothBinder binder = (BluetoothService.BluetoothBinder) iBinder;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.mapView);

//        int[][] tileStatus = new int[15][20];
//        for (int x = 0; x < 15; x++) {
//            for (int y = 0; y < 20; y++) {
//                tileStatus[x][y] = MapView.STATUS_ROBOT;
//            }
//        }
//        mapView.setTileStatus(tileStatus);

        ToggleButton robotToggle = (ToggleButton) findViewById(R.id.robotToggle);
        robotToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Button buttonRotateAnti = (Button) findViewById(R.id.buttonRotateAnti);
                Button buttonRotateClock = (Button) findViewById(R.id.buttonRotateClock);
                if (isChecked) {
                    buttonRotateAnti.setEnabled(true);
                    buttonRotateClock.setEnabled(true);
                    mapView.enableTouchRobot();
                } else {
                    buttonRotateAnti.setEnabled(false);
                    buttonRotateClock.setEnabled(false);
                    mapView.disableTouchRobot();
                }
            }
        });


        bluetoothHelper = new BluetoothHelper();
        bluetoothIntent = new Intent(this, BluetoothService.class);
        startService(bluetoothIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (bluetoothIsStarted) {
            bindService(bluetoothIntent, mConnection, BIND_ABOVE_CLIENT);
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        if (bluetoothConnected) {
            menu.findItem(R.id.action_bluetooth_connect).setVisible(false);
            menu.findItem(R.id.action_bluetooth_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.action_bluetooth_disconnect).setVisible(false);
            menu.findItem(R.id.action_bluetooth_connect).setVisible(true);
        }
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
            case R.id.action_bluetooth_disconnect:
                if (mBound) {
                    mService.disconnectBtSocket();
                }
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

    public void rotateRobotAnti(View view) {
        mapView.rotateRobotAnti();
    }

    public void rotateRobotClock(View view) {
        mapView.rotateRobotClock();
    }

    public void explore(View view) {
        findViewById(R.id.waypointToggle).setEnabled(true);
        findViewById(R.id.fastestPathButton).setEnabled(true);
    }

    private void openBtDialog() {
        if (!bluetoothIsStarted) {
            bindService(bluetoothIntent, mConnection, BIND_ABOVE_CLIENT);
            bluetoothIsStarted = true;
        }
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

        // finally, show dialog
        btDialog = BtDialogFragment.newInstance(bluetoothHelper.getBluetoothAdapter());
        btDialog.show(ft, "btDialog");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(BluetoothEvent event) {
        switch (event.status) {
            case BluetoothEvent.CONNECTED:
                Toast.makeText(this, "Connected to device", Toast.LENGTH_SHORT).show();
                if (btDialog.isVisible()) {
                    Log.d(TAG, "Dialog is visible");
                    btDialog.dismiss();
                }
                bluetoothConnected = true;
                invalidateOptionsMenu();
                break;
            case BluetoothEvent.DISCONNECTED:
                Toast.makeText(this, "Disconnected from device", Toast.LENGTH_SHORT).show();
                bluetoothConnected = false;
                invalidateOptionsMenu();
                break;
            case BluetoothEvent.MESSAGE_RECEIVED:
                Toast.makeText(this, "Message: " + event.message, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
