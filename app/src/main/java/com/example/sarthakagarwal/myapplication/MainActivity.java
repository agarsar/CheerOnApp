package com.example.sarthakagarwal.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("OnCreate","Calling On Create from Main Activity");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
