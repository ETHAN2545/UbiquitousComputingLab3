package com.example.serviceactivity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MotionService extends Service implements SensorEventListener {

    private static final String TAG = "MotionService";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float lastX;
    private float lastY;
    private float lastZ;

    private boolean firstReading = true;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service created");

        sensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        if (accelerometer == null) {
            Log.d(TAG, "Accelerometer not available");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Service started");

        if (accelerometer != null) {

            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if (firstReading) {

                lastX = x;
                lastY = y;
                lastZ = z;

                firstReading = false;

            } else {

                float differenceX = Math.abs(x - lastX);
                float differenceY = Math.abs(y - lastY);
                float differenceZ = Math.abs(z - lastZ);

                if (differenceX > 3 || differenceY > 3 || differenceZ > 3) {
                    Log.d(TAG, "Motion detected");
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        
        super.onDestroy();

        Log.d(TAG, "Service destroyed");
    }
}
