package com.example.serviceactivity;

import android.app.Service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;

import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import android.util.Log;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MotionService extends Service implements SensorEventListener {

    private static final String TAG = "MotionService";

    private static final String CHANNEL_ID = "motion_channel";
    private static final int NOTIFICATION_ID = 1;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private volatile float currentX;
    private volatile float currentY;
    private volatile float currentZ;

    private Thread motionThread;
    private volatile boolean running = false;

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Service created");

        sensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        createNotificationChannel();

        if (accelerometer == null) {
            Log.d(TAG, "Accelerometer not available");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Service started");

        if (accelerometer != null && !running) {

            running = true;

            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            motionThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    float lastX = 0;
                    float lastY = 0;
                    float lastZ = 0;

                    boolean firstReading = true;

                    long lastNotificationTime = 0;

                    Log.d(TAG, "Motion thread started");

                    while (running) {

                        float x = currentX;
                        float y = currentY;
                        float z = currentZ;

                        if (!firstReading) {

                            float differenceX = Math.abs(x - lastX);

                            float differenceY = Math.abs(y - lastY);

                            float differenceZ = Math.abs(z - lastZ);

                            long currentTime = System.currentTimeMillis();

                            if ((differenceX > 3 || differenceY > 3 || differenceZ > 3) && currentTime - lastNotificationTime > 3000) {

                                Log.d(TAG, "Motion detected");

                                showMotionNotification();

                                lastNotificationTime = currentTime;
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

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, "Motion Detection", NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription("Notifications when phone movement is detected");

            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showMotionNotification() {

        String motionTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        Intent intent = new Intent(MotionService.this, MotionDetailsActivity.class);

        intent.putExtra("motionTime", motionTime);

        PendingIntent pendingIntent = PendingIntent.getActivity(MotionService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder = new Notification.Builder(MotionService.this, CHANNEL_ID);

        } else {

            builder = new Notification.Builder(MotionService.this);

            builder.setPriority(Notification.PRIORITY_HIGH);
        }

        Notification notification = builder
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Motion Detected!")
                .setContentText("Your phone moved at " + motionTime)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);

        Log.d(TAG, "Notification displayed at " + motionTime);
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