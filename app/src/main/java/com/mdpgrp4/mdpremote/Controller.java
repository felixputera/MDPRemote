package com.mdpgrp4.mdpremote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Controller extends AppCompatActivity {


    //Buttons
    private Button function_a_btn;
    private Button function_b_btn;
    private Button forwardBtn;
    private Button reverseBtn;
    private Button leftBtn;
    private Button rightBtn;
    private Button saveBtn;
    private Button loadBtn;

    //EditText
    private EditText function_a_edit;
    private EditText function_b_edit;

    //Shared Preferences
    public static final String DEFAULT="N/A";
    String functionA,functionB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        loadBtn =  (Button) findViewById(R.id.loadBtn);
        function_a_edit = (EditText) findViewById(R.id.function_a_edit);
        function_b_edit = (EditText) findViewById(R.id.function_b_edit);
        load(findViewById(R.id.loadBtn));

    }
    public void save(View view) {

        SharedPreferences sp = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editSP = sp.edit();

        editSP.putString("functionA", function_a_edit.getText().toString());
        editSP.putString("functionB", function_b_edit.getText().toString());

        editSP.commit();


        Toast.makeText(this, "Saved Changes Successfully", Toast.LENGTH_SHORT).show();
    }

    //to load shared preferences
    public void load(View view){
        SharedPreferences sp = getSharedPreferences("MyData", Context.MODE_PRIVATE);
        functionA = sp.getString("functionA", DEFAULT);
        functionB = sp.getString("functionB", DEFAULT);
        if(functionA.equals(DEFAULT) && functionB.equals(DEFAULT)){
            Toast.makeText(this, "No Function Data Found", Toast.LENGTH_SHORT).show();
            function_a_edit.setText(DEFAULT);
            function_b_edit.setText(DEFAULT);
        }else{
            Toast.makeText(this, "Function Loaded", Toast.LENGTH_SHORT).show();
            function_a_edit.setText(functionA);
            function_b_edit.setText(functionB);
        }
    }
}
