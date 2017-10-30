package com.mdpgrp4.mdpremote

import android.Manifest
import android.app.Activity
import android.app.DialogFragment
import android.bluetooth.BluetoothAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import com.mdpgrp4.mdpremote.BluetoothDialog.BtDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity() {

    private val TAG = "MAIN_ACTIVITY"
    internal var mService: BluetoothService? = null
    internal var mBound = false
    private var bluetoothIntent: Intent? = null
    private var mapView: MapView? = null
    private var bluetoothHelper: BluetoothHelper? = null
    private var btDialog: DialogFragment? = null
    private var bluetoothIsStarted = false
    private var bluetoothConnected = false
    private var statusView: TextView? = null
    private var buttonExplore: Button? = null
    private var robotToggle: ToggleButton? = null
    private var mapAuto = true
    private val mapBuffer = arrayOfNulls<String>(2)
    private val robotPosBuffer = arrayOf("1", "1", "N")

    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            val binder = iBinder as BluetoothService.BluetoothBinder
            mService = binder.service
            mBound = true
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusView = findViewById<View>(R.id.statusTextView) as TextView
        mapView = findViewById<View>(R.id.mapView) as MapView

        buttonExplore = findViewById<View>(R.id.exploreButton) as Button
        robotToggle = findViewById<View>(R.id.robotToggle) as ToggleButton
        robotToggle!!.setOnCheckedChangeListener { buttonView, isChecked ->
            val buttonRotateAnti = findViewById<View>(R.id.buttonRotateAnti) as Button
            val buttonRotateClock = findViewById<View>(R.id.buttonRotateClock) as Button
            if (isChecked) {
                buttonRotateAnti.isEnabled = true
                buttonRotateClock.isEnabled = true
                buttonExplore!!.isEnabled = false
                mapView!!.enableTouchRobot()
            } else {
                buttonRotateAnti.isEnabled = false
                buttonRotateClock.isEnabled = false
                buttonExplore!!.isEnabled = true
                mapView!!.disableTouchRobot()
            }
        }

        val mapToggle = findViewById<View>(R.id.mapToggle) as ToggleButton
        mapToggle.setOnCheckedChangeListener { buttonView, isChecked ->
            val buttonRefreshMap = findViewById<View>(R.id.refreshMapButton) as Button
            if (isChecked) {
                buttonRefreshMap.isEnabled = true
                mapAuto = false
            } else {
                buttonRefreshMap.isEnabled = false
                mapAuto = true
            }
        }

        val waypointToggle = findViewById<View>(R.id.waypointToggle) as ToggleButton
        waypointToggle.setOnCheckedChangeListener { buttonView, isChecked ->
            val fastestPathButton = findViewById<View>(R.id.fastestPathButton) as Button
            if (isChecked) {
                mapView!!.enableTouchWaypoint()
                fastestPathButton.isEnabled = false
            } else {
                val waypoint = mapView!!.disableTouchWaypoint()
                if (mBound) {
                    mService!!.writeBtOut("pcwaypoint," + waypoint[0] + "," + waypoint[1])
                }
                fastestPathButton.isEnabled = true
            }
        }


        bluetoothHelper = BluetoothHelper()
        bluetoothIntent = Intent(this, BluetoothService::class.java)
        startService(bluetoothIntent)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        if (bluetoothIsStarted) {
            bindService(bluetoothIntent, mConnection, Context.BIND_ABOVE_CLIENT)
        }
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        if (mBound) {
            unbindService(mConnection)
            mBound = false
        }
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        if (bluetoothConnected) {
            menu.findItem(R.id.action_bluetooth_connect).isVisible = false
            menu.findItem(R.id.action_bluetooth_disconnect).isVisible = true
        } else {
            menu.findItem(R.id.action_bluetooth_disconnect).isVisible = false
            menu.findItem(R.id.action_bluetooth_connect).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_tilt_steering -> {
                val tilt_steering_intent = Intent(this, TiltSteeringActivity::class.java)
                startActivity(tilt_steering_intent)
                return true
            }
            R.id.controller -> {
                val controller = Intent(this, ControllerActivity::class.java)
                startActivity(controller)
                return true
            }
            R.id.action_bluetooth_connect -> {
                enableBluetooth()
                return true
            }
            R.id.action_bluetooth_disconnect -> {
                if (mBound) {
                    mService!!.disconnectBtSocket()
                }
                return true
            }
            R.id.action_reset -> {
                mapView!!.setMapDescriptor(null,
                        "000000000000000000000000000000000000000000000000000000000000000000000000000")
                robotToggle!!.isEnabled = true
                findViewById<View>(R.id.fastestPathButton).isEnabled = false
                mapView!!.setRobotPos(arrayOf("1", "1", "N"))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun enableBluetooth() {
        if (!bluetoothHelper!!.bluetoothAdapter.isEnabled && bluetoothHelper!!.bluetoothIsAvailable()) {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableIntent, BluetoothHelper.REQUEST_ENABLE_BT)
        } else {
            openBtDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == BluetoothHelper.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                openBtDialog()
            }
        }
    }

    fun rotateRobotAnti(view: View) {
        mapView!!.rotateRobotAnti()
    }

    fun rotateRobotClock(view: View) {
        mapView!!.rotateRobotClock()
    }

    fun mapRefresh(view: View) {
        mapView!!.setMapDescriptor(mapBuffer[0], mapBuffer[1])
        mapView!!.setRobotPos(robotPosBuffer)
    }

    fun explore(view: View) {
        findViewById<View>(R.id.fastestPathButton).isEnabled = true
        findViewById<View>(R.id.robotToggle).isEnabled = false
        //        int[][] tileStatus = new int[15][20];
        //        for (int x = 0; x < 15; x++) {
        //            for (int y = 0; y < 20; y++) {
        //                tileStatus[x][y] = MapView.STATUS_EMPTY;
        //            }
        //        }
        //        mapView.setTileStatus(tileStatus);
        if (mBound) {
            mService!!.writeBtOut("pcbeginExploration")
        }
    }

    fun fastestPath(view: View) {
        if (mBound) {
            mService!!.writeBtOut("pcfastestPath")
        }
    }

    private fun openBtDialog() {
        if (!bluetoothIsStarted) {
            bindService(bluetoothIntent, mConnection, Context.BIND_ABOVE_CLIENT)
            bluetoothIsStarted = true
        }
        val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION)
        val ft = fragmentManager.beginTransaction()
        val prev = fragmentManager.findFragmentByTag("btDialog")
        if (prev != null) {
            ft.remove(prev)
        }
        ft.addToBackStack(null)

        // finally, show dialog
        btDialog = BtDialogFragment.newInstance(bluetoothHelper!!.bluetoothAdapter, mService)
        btDialog!!.show(ft, "btDialog")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBluetoothEvent(event: MessageEvent) {
        when (event.status) {
            MessageEvent.CONNECTED -> {
                Toast.makeText(this, "Connected to device", Toast.LENGTH_SHORT).show()
                if (btDialog!!.isVisible) {
                    Log.d(TAG, "Dialog is visible")
                    btDialog!!.dismiss()
                }
                bluetoothConnected = true
                invalidateOptionsMenu()
                statusView!!.text = "Connected"
            }
            MessageEvent.DISCONNECTED -> {
                Toast.makeText(this, "Disconnected from device", Toast.LENGTH_SHORT).show()
                bluetoothConnected = false
                invalidateOptionsMenu()
                statusView!!.text = "Disconnected"
            }
            MessageEvent.ROBOT_STATUS -> statusView!!.text = event.message
            MessageEvent.ROBOT_POS -> {
                if (mapAuto) {
                    mapView!!.setRobotPos(event.coordinates)
                }
                robotPosBuffer[0] = event.coordinates[0]
                robotPosBuffer[1] = event.coordinates[1]
                robotPosBuffer[2] = event.coordinates[2]
            }
            MessageEvent.MAP -> {
                if (mapAuto) {
                    mapView!!.setMapDescriptor(event.map[0], event.map[1])
                }
                mapBuffer[0] = event.map[0]
                mapBuffer[1] = event.map[1]
            }
            MessageEvent.CONNECT_DEVICE -> mService!!.connectDevice(event.device)
            MessageEvent.CONNECT_FAIL -> Toast.makeText(this, "Failed to connect to device", Toast.LENGTH_SHORT).show()
            MessageEvent.INVALID_JSON -> Toast.makeText(this, "Incoming message is not in valid JSON format", Toast.LENGTH_SHORT).show()
        }
    }
}
