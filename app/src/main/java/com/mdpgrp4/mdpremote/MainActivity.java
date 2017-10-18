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
import android.widget.TextView;
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
    private TextView statusView;
    private Button buttonExplore;
    private ToggleButton robotToggle;
    private boolean mapAuto = true;
    private String[] mapBuffer = new String[2];
    private String[] robotPosBuffer = {"1", "1", "N"};

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

        statusView = (TextView) findViewById(R.id.statusTextView);
        mapView = (MapView) findViewById(R.id.mapView);

//        int[][] tileStatus = new int[15][20];
//        for (int x = 0; x < 15; x++) {z
//            for (int y = 0; y < 20; y++) {
//                tileStatus[x][y] = MapView.STATUS_ROBOT;
//            }
//        }
//        mapView.setTileStatus(tileStatus);

        buttonExplore = (Button) findViewById(R.id.exploreButton);
        robotToggle = (ToggleButton) findViewById(R.id.robotToggle);
        robotToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Button buttonRotateAnti = (Button) findViewById(R.id.buttonRotateAnti);
                Button buttonRotateClock = (Button) findViewById(R.id.buttonRotateClock);
                if (isChecked) {
                    buttonRotateAnti.setEnabled(true);
                    buttonRotateClock.setEnabled(true);
                    buttonExplore.setEnabled(false);
                    mapView.enableTouchRobot();
                } else {
                    buttonRotateAnti.setEnabled(false);
                    buttonRotateClock.setEnabled(false);
                    buttonExplore.setEnabled(true);
                    mapView.disableTouchRobot();
                }
            }
        });

        ToggleButton mapToggle = (ToggleButton) findViewById(R.id.mapToggle);
        mapToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Button buttonRefreshMap = (Button) findViewById(R.id.refreshMapButton);
                if (isChecked) {
                    buttonRefreshMap.setEnabled(true);
                    mapAuto = false;
                } else {
                    buttonRefreshMap.setEnabled(false);
                    mapAuto = true;
                }
            }
        });

        ToggleButton waypointToggle = (ToggleButton) findViewById(R.id.waypointToggle);
        waypointToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Button fastestPathButton = (Button) findViewById(R.id.fastestPathButton);
                if (isChecked) {
                    mapView.enableTouchWaypoint();
                    fastestPathButton.setEnabled(false);
                } else {
                    int[] waypoint = mapView.disableTouchWaypoint();
                    if (mBound) {
                        mService.writeBtOut("pcwaypoint," + waypoint[0] + "," + waypoint[1]);
                    }
                    fastestPathButton.setEnabled(true);
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
            case R.id.controller:
                Intent controller = new Intent(this, ControllerActivity.class);
                startActivity(controller);
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

    public void mapRefresh(View view) {
        mapView.setMapDescriptor(mapBuffer[0], mapBuffer[1]);
        mapView.setRobotPos(robotPosBuffer);
    }

    public void explore(View view) {
        findViewById(R.id.waypointToggle).setEnabled(true);
        findViewById(R.id.fastestPathButton).setEnabled(true);
        findViewById(R.id.robotToggle).setEnabled(false);
//        int[][] tileStatus = new int[15][20];
//        for (int x = 0; x < 15; x++) {
//            for (int y = 0; y < 20; y++) {
//                tileStatus[x][y] = MapView.STATUS_EMPTY;
//            }
//        }
//        mapView.setTileStatus(tileStatus);
        if (mBound) {
            mService.writeBtOut("pcbeginExploration");
        }
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
        btDialog = BtDialogFragment.newInstance(bluetoothHelper.getBluetoothAdapter(), mService);
        btDialog.show(ft, "btDialog");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(MessageEvent event) {
        switch (event.status) {
            case MessageEvent.CONNECTED:
                Toast.makeText(this, "Connected to device", Toast.LENGTH_SHORT).show();
                if (btDialog.isVisible()) {
                    Log.d(TAG, "Dialog is visible");
                    btDialog.dismiss();
                }
                bluetoothConnected = true;
                invalidateOptionsMenu();
                statusView.setText("Connected");
                break;
            case MessageEvent.DISCONNECTED:
                Toast.makeText(this, "Disconnected from device", Toast.LENGTH_SHORT).show();
                bluetoothConnected = false;
                invalidateOptionsMenu();
                statusView.setText("Disconnected");
                break;
            case MessageEvent.ROBOT_STATUS:
                statusView.setText(event.message);
                break;
            case MessageEvent.ROBOT_POS:
                if (mapAuto) {
                    mapView.setRobotPos(event.coordinates);
                }
                robotPosBuffer[0] = event.coordinates[0];
                robotPosBuffer[1] = event.coordinates[1];
                robotPosBuffer[2] = event.coordinates[2];

                break;
            case MessageEvent.MAP:
                if (mapAuto) {
                    mapView.setMapDescriptor(event.map[0], event.map[1]);
                }
                mapBuffer[0] = event.map[0];
                mapBuffer[1] = event.map[1];
                break;
            case MessageEvent.CONNECT_DEVICE:
                mService.connectDevice(event.device);
                break;
            case MessageEvent.CONNECT_FAIL:
                Toast.makeText(this, "Failed to connect to device", Toast.LENGTH_SHORT).show();
                break;
            case MessageEvent.INVALID_JSON:
                Toast.makeText(this, "Incoming message is not in valid JSON format", Toast.LENGTH_SHORT).show();
        }
    }
}
