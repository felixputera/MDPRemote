package com.mdpgrp4.mdpremote

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ControllerActivity : AppCompatActivity() {
    internal var mService: BluetoothService? = null
    internal var mBound = false
    internal var functionA: String? = null
    internal var functionB: String? = null
    //Buttons
    private var functionABtn: Button? = null
    private var functionBBtn: Button? = null
    private var forwardBtn: Button? = null
    private var reverseBtn: Button? = null
    private var leftBtn: Button? = null
    private var rightBtn: Button? = null
    private var functionBSave: Button? = null
    private var functionASave: Button? = null
    //EditText
    private var functionAEdit: EditText? = null
    private var functionBEdit: EditText? = null

    private var statusView: TextView? = null

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
        setContentView(R.layout.activity_controller)

        statusView = findViewById<View>(R.id.statusTextView) as TextView

        functionASave = findViewById<View>(R.id.functionASave) as Button
        functionBSave = findViewById<View>(R.id.functionBSave) as Button
        functionABtn = findViewById<View>(R.id.functionABtn) as Button
        functionBBtn = findViewById<View>(R.id.functionBBtn) as Button
        forwardBtn = findViewById<View>(R.id.forwardBtn) as Button
        reverseBtn = findViewById<View>(R.id.reverseBtn) as Button
        leftBtn = findViewById<View>(R.id.leftBtn) as Button
        rightBtn = findViewById<View>(R.id.rightBtn) as Button

        functionAEdit = findViewById<View>(R.id.functionAEdit) as EditText
        functionBEdit = findViewById<View>(R.id.functionBEdit) as EditText

        functionASave!!.setOnClickListener { saveFunctionData(functionAEdit, "function A") }
        functionBSave!!.setOnClickListener { saveFunctionData(functionBEdit, "function B") }

        loadFunctionData()

        functionABtn!!.setOnClickListener {
            if (mBound) {
                mService!!.writeBtOut(functionAEdit!!.text.toString())
            }
        }
        functionBBtn!!.setOnClickListener {
            if (mBound) {
                mService!!.writeBtOut(functionBEdit!!.text.toString())
            }
        }
        forwardBtn!!.setOnClickListener {
            if (mBound) {
                mService!!.writeBtOut("sr17")
                statusView!!.text = "Moving forward"
            }
        }
        reverseBtn!!.setOnClickListener {
            if (mBound) {
                mService!!.writeBtOut("sr64")
                statusView!!.text = "Reversing"
            }
        }
        leftBtn!!.setOnClickListener {
            if (mBound) {
                mService!!.writeBtOut("sr32")
                statusView!!.text = "Turning left"
            }
        }
        rightBtn!!.setOnClickListener {
            if (mBound) {
                mService!!.writeBtOut("sr48")
                statusView!!.text = "Turning right"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
        val bluetoothIntent = Intent(this, BluetoothService::class.java)
        bindService(bluetoothIntent, mConnection, Context.BIND_ABOVE_CLIENT)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        if (mBound) {
            unbindService(mConnection)
            mBound = false
        }
        super.onStop()
    }

    fun saveFunctionData(text: EditText?, name: String) {

        val sp = getSharedPreferences("MyData", Context.MODE_PRIVATE)
        val editSP = sp.edit()

        editSP.putString(name, text!!.text.toString())

        editSP.apply()

        Toast.makeText(this, "Saved $name successfully", Toast.LENGTH_SHORT).show()
    }

    //to load shared preferences
    fun loadFunctionData() {
        val sp = getSharedPreferences("MyData", Context.MODE_PRIVATE)
        functionA = sp.getString("function A", DEFAULT)
        functionB = sp.getString("function B", DEFAULT)
        if (functionA == DEFAULT && functionB == DEFAULT) {
            Toast.makeText(this, "No functions data found", Toast.LENGTH_SHORT).show()
            functionAEdit!!.setText(DEFAULT)
            functionBEdit!!.setText(DEFAULT)
        } else {
            Toast.makeText(this, "Functions loaded", Toast.LENGTH_SHORT).show()
            functionAEdit!!.setText(functionA)
            functionBEdit!!.setText(functionB)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onBluetoothEvent(event: MessageEvent) {
        when (event.status) {
            MessageEvent.DISCONNECTED -> finish()
            MessageEvent.ROBOT_STATUS -> statusView!!.text = event.message
        }
    }

    companion object {


        //Shared Preferences
        val DEFAULT = "N/A"
    }
}
