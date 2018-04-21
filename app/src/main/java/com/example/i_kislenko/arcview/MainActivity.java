package com.example.i_kislenko.arcview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ((ArcView) findViewById(R.id.arcView)).setPercentage(0.5f);
        ((ArcView) findViewById(R.id.arcViewWithCustomAngle)).setPercentage(0.3f);
        ((ArcView) findViewById(R.id.littleArcView)).setPercentage(0.85f);
        ((ArcView) findViewById(R.id.arcViewWithCustomColor)).setPercentage(0.78f);
        ((ArcView) findViewById(R.id.arcViewWithWeightSupport)).setPercentage(1.0f);
    }
}
