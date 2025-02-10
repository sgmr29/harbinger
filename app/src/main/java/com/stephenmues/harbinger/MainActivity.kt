package com.stephenmues.harbinger

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.Manifest


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
        requestPermissions(arrayOf(Manifest.permission.READ_SMS), 103)

        val channel = NotificationChannel(
            "harbinger_post",
            "Harbinger Post Service",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)

        val startButton: Button = findViewById(R.id.startButton)
        val stopButton: Button = findViewById(R.id.stopButton)

        startButton.setOnClickListener {
            //startService(Intent(this, WebServerService::class.java))
            val serviceIntent = Intent(
                this,
                PostService::class.java
            )
            startForegroundService(serviceIntent)

        }

        stopButton.setOnClickListener {
            stopService(Intent(this, PostService::class.java))
        }
    }
}