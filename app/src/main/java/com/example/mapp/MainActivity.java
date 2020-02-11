package com.example.mapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolBar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(myToolBar);
        myToolBar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        myToolBar.setTitle("Mapp");

    }
}
