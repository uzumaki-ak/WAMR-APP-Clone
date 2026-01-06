package com.wamr.recovery.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.wamr.recovery.MainActivity
import com.wamr.recovery.R

class ForegroundService : Service() {

    private val CHANNEL_ID = "wamr_service_channel"
    private val NOTIFICATION_ID = 1001

    private lateinit var mediaScanner: MediaScanner
    private lateinit var statusDownloader: StatusDownloader

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaScanner = MediaScanner(this)
        statusDownloader = StatusDownloader(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        mediaScanner.startMonitoring()
        statusDownloader.startMonitoring()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaScanner.stopMonitoring()
        statusDownloader.stopMonitoring()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WAMR Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps WAMR running in background"
                setShowBadge(false)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WAMR is running")
            .setContentText("Monitoring messages and media")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}