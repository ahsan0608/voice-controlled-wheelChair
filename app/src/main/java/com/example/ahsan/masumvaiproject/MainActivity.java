package com.example.ahsan.masumvaiproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mPatient, mNurse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        mPatient = (Button) findViewById(R.id.login);
        mNurse = (Button) findViewById(R.id.registration);


//        mPatient.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, PatientsLoginActivity.class);
//                startActivity(intent);
//            }
//        });



        mPatient.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        mPatient.getBackground().setAlpha(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        mPatient.getBackground().setAlpha(255);
                        Intent intent = new Intent(MainActivity.this, PatientsLoginActivity.class);
                        startActivity(intent);

                }
                return false;
            }
        });






        mNurse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:

                        mNurse.getBackground().setAlpha(100);
                        break;
                    case MotionEvent.ACTION_UP:
                        mNurse.getBackground().setAlpha(255);
                        Intent intent = new Intent(MainActivity.this, HelpingHandLoginActivity.class);
                        startActivity(intent);

                }
                return false;
            }
        });














//
//        mNurse.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, HelpingHandLoginActivity.class);
//                startActivity(intent);
//            }
//        });


    }
}
