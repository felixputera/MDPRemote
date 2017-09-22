package com.mdpgrp4.mdpremote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Color;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

public class Controller extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        final Button forward = (Button) findViewById(R.id.ctrForward);
        final Button reverse = (Button) findViewById(R.id.ctrReverse);
        final Button left = (Button) findViewById(R.id.ctrLeft);
        final Button right = (Button) findViewById(R.id.ctrRight);
        final Button explore = (Button) findViewById(R.id.ctrExplore);
        final Button path = (Button) findViewById(R.id.ctrPath);
        final Button strafeLeft = (Button) findViewById(R.id.ctrStrafeLeft);
        final Button strafeRight = (Button) findViewById(R.id.ctrStrafeRight);

        strafeLeft.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick (View v){
                        TextView curCmd = (TextView) findViewById(R.id.curCmd);
                        curCmd.setText("strafeLeft");
                    }
                }
        );
        strafeRight.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick (View v){
                        TextView curCmd = (TextView) findViewById(R.id.curCmd);
                        curCmd.setText("strafeRight");
                    }
                }
        );
        /**
         reverse = (Button) findViewById(R.id.reverse);
         left = (Button) findViewById(R.id.left);
         right = (Button) findViewById(R.id.right);
         path = (Button) findViewById(R.id.path);
         explore = (Button) findViewById(R.id.explore);
         path = (Button) findViewById(R.id.path);
         strafeLeft = (Button) findViewById(R.id.strafeLeft);
         strafeRight = (Button) findViewById(R.id.strafeRight);
         **/
    }
}
