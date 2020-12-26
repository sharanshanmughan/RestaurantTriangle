package com.auggen.restuaranttriangle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class ResultViewActivity extends AppCompatActivity {
    private TextView areaLabel1,timeLabel1,timeLabel2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_view);

        areaLabel1 = findViewById(R.id.area1);
        timeLabel1 = findViewById(R.id.time1);
        timeLabel2 = (TextView)findViewById(R.id.time2);
        Intent intent = getIntent();
        final String area1 = intent.getStringExtra("Area1");
        areaLabel1.setText("Area : "+area1+" Km^2");
        final String time1 = intent.getStringExtra("Time1");
        timeLabel1.setText("Time : "+time1+" Hr");
        final String time2 = intent.getStringExtra("Time2");
        timeLabel2.setText("Time : "+time2+" Hr");
    }
}
