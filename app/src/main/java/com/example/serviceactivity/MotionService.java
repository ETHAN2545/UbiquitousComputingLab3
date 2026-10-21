package com.example.serviceactivity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MotionService extends Service
        implements SensorEventListener {

    private static final String TAG = "MotionService";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private volatile float currentX;
    private volatile float currentY;
    private volatile float currentZ;

    private Thread motionThread;
    private volatile boolean running = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service created");

        sensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer == null) {
            Log.d(TAG, "Accelerometer not available");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Service started");

        if (accelerometer != null && !running) {

            running = true;

            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL
            );

            motionThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    float lastX = 0;
                    float lastY = 0;
                    float lastZ = 0;

                    boolean firstReading = true;

                    Log.d(TAG, "Motion thread started");

                    while (running) {

                        float x = currentX;
                        float y = currentY;
                        float z = currentZ;

                        if (!firstReading) {

                            float differenceX = Math.abs(x - lastX);

                            float differenceY = Math.abs(y - lastY);

                            float differenceZ = Math.abs(z - lastZ);

                            if (differenceX > 3 || differenceY > 3 || differenceZ > 3) {

                                Log.d(TAG, "Motion detected");

                            }
                        }

                        lastX = x;
                        lastY = y;
                        lastZ = z;

                        firstReading = false;

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }

                    Log.d(TAG, "Motion thread stopped");
                }
            });

            motionThread.start();
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            currentX = event.values[0];
            currentY = event.values[1];
            currentZ = event.values[2];
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

        running = false;

        if (motionThread != null) {
            motionThread.interrupt();
        }

        sensorManager.unregisterListener(this);

        super.onDestroy();

        Log.d(TAG, "Service destroyed");
    }
}