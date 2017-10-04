package com.mdpgrp4.mdpremote;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ControllerActivity extends AppCompatActivity {


    //Shared Preferences
    public static final String DEFAULT = "N/A";
    BluetoothService mService;
    boolean mBound = false;
    String functionA, functionB;
    //Buttons
    private Button functionABtn;
    private Button functionBBtn;
    private Button forwardBtn;
    private Button reverseBtn;
    private Button leftBtn;
    private Button rightBtn;
    private Button functionBSave;
    private Button functionASave;
    //EditText
    private EditText functionAEdit;
    private EditText functionBEdit;
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
        setContentView(R.layout.activity_controller);

        functionASave = (Button) findViewById(R.id.functionASave);
        functionBSave = (Button) findViewById(R.id.functionBSave);
        functionABtn = (Button) findViewById(R.id.functionABtn);
        functionBBtn = (Button) findViewById(R.id.functionBBtn);
        forwardBtn = (Button) findViewById(R.id.forwardBtn);
        reverseBtn = (Button) findViewById(R.id.reverseBtn);
        leftBtn = (Button) findViewById(R.id.leftBtn);
        rightBtn = (Button) findViewById(R.id.rightBtn);

        functionAEdit = (EditText) findViewById(R.id.functionAEdit);
        functionBEdit = (EditText) findViewById(R.id.functionBEdit);

        functionASave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFunctionData(functionAEdit, "function A");
            }
        });
        functionBSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFunctionData(functionBEdit, "function B");
            }
        });

        loadFunctionData();

        functionABtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    mService.writeBtOut(functionAEdit.getText().toString());
                }
            }
        });
        functionBBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    mService.writeBtOut(functionBEdit.getText().toString());
                }
            }
        });
        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    mService.writeBtOut("f");
                }
            }
        });
        reverseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    mService.writeBtOut("r");
                }
            }
        });
        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    mService.writeBtOut("tl");
                }
            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBound) {
                    mService.writeBtOut("tr");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        Intent bluetoothIntent = new Intent(this, BluetoothService.class);
        bindService(bluetoothIntent, mConnection, BIND_ABOVE_CLIENT);
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

    public void saveFunctionData(EditText text, String name) {

        SharedPreferences sp = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editSP = sp.edit();

        editSP.putString(name, text.getText().toString());

        editSP.apply();

        Toast.makeText(this, "Saved " + name + " successfully", Toast.LENGTH_SHORT).show();
    }

    //to load shared preferences
    public void loadFunctionData() {
        SharedPreferences sp = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        functionA = sp.getString("function A", DEFAULT);
        functionB = sp.getString("function B", DEFAULT);
        if(functionA.equals(DEFAULT) && functionB.equals(DEFAULT)){
            Toast.makeText(this, "No functions data found", Toast.LENGTH_SHORT).show();
            functionAEdit.setText(DEFAULT);
            functionBEdit.setText(DEFAULT);
        }else{
            Toast.makeText(this, "Functions loaded", Toast.LENGTH_SHORT).show();
            functionAEdit.setText(functionA);
            functionBEdit.setText(functionB);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(MessageEvent event) {
        switch (event.status) {
            case MessageEvent.DISCONNECTED:
                finish();
                break;
            case MessageEvent.ROBOT_STATUS:
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
