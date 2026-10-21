package com.example.serviceactivity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MotionDetailsActivity extends AppCompatActivity {

    TextView motionText;
    TextView timeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_details);

        motionText = findViewById(R.id.motionText);
        timeText = findViewById(R.id.timeText);

        String motionTime = getIntent().getStringExtra("motionTime");

        motionText.setText("Motion Detected!");

        timeText.setText("Time: " + motionTime);
    }
}
