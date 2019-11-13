package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private int mCount;
    TextView mDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDisplay = findViewById(R.id.mDisplay);
        mCount=0;
        mDisplay.setText(String.format("%d",mDisplay));
    }


    public void testonclick(View view) {
        mCount++;
        if ( mDisplay != null) mDisplay.setText(String.format("%d",mDisplay));
    }
}
