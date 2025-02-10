package com.stephenmues.harbinger;

import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC;

import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.Manifest;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;
import java.net.*;

public class PostService extends Service {
    private Timer timer;
    private static final long INTERVAL = 30000; // 30 seconds

    private int smsCount = -2;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(1, createNotification(), FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        startSendingRequests();
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "harbinger_post")
                .setContentTitle("Harbinger")
                .setContentText("Keeping github in sync")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        return builder.build();
    }

    private void startSendingRequests() {
        timer = new Timer();

        // Check every 30 seconds, and send if the count changed
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                postCountIfChanged();
            }
        }, 0, INTERVAL);

        // Send hourly (regardless of if it changed)
        long HOURLY_INTERVAL = 1000 * 60 * 60;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                postCurrentCount();
            }
        }, HOURLY_INTERVAL, HOURLY_INTERVAL);
    }

    private void postCountIfChanged() {
        boolean sendNow = false;
        int newSmsCount = getUnreadSmsCount();
        if (newSmsCount != smsCount) {
            smsCount = newSmsCount;
            sendNow = true;
        }
        if (sendNow) {
            postCurrentCount();
        }
    }
    private void postCurrentCount() {
        new Thread(() -> {
            try {
                // See https://docs.github.com/en/rest/issues/issues?apiVersion=2022-11-28#update-an-issue

                URL url = new URL("https://api.github.com/repos/sgmr29/harbinger/issues/1");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PATCH");
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setRequestProperty("Authorization", "Bearer " + Secrets.githubToken);
                conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Create JSON payload
                JSONObject json = new JSONObject();
                json.put("title", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
                json.put("body", String.valueOf(smsCount));

                // Send
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                System.out.println("Response Code: " + responseCode);

                conn.disconnect();
            } catch(Exception e){
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getUnreadSmsCount() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            return -1;
        }
        Uri uri = Uri.parse("content://sms/inbox");
        String[] projection = {"_id"};
        Cursor cursor = getContentResolver().query(uri, projection, "read = 0", null, null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();
        }
        return count;
    }
}
