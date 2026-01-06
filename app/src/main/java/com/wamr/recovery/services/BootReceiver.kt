package com.wamr.recovery.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Boot completed, starting service")

            if (NotificationListener.isEnabled(context)) {
                val serviceIntent = Intent(context, ForegroundService::class.java)
                context.startForegroundService(serviceIntent)
                Log.d(TAG, "Service started after boot")
            } else {
                Log.w(TAG, "Notification access not enabled, skipping service start")
            }
        }
    }
}