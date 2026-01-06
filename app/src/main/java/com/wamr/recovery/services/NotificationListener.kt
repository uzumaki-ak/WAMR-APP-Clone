package com.wamr.recovery.services

import android.app.Notification
import android.content.Context
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.wamr.recovery.database.AppDatabase
import com.wamr.recovery.database.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationListener : NotificationListenerService() {

    private val TAG = "NotificationListener"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database by lazy { AppDatabase.getDatabase(this) }

    private val targetApps = setOf(
        "com.whatsapp",
        "org.telegram.messenger",
        "com.facebook.orca",
        "com.instagram.android"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName

        if (packageName !in targetApps) return

        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text

        val isDeleted = text.contains("This message was deleted", ignoreCase = true) ||
                text.contains("deleted this message", ignoreCase = true)

        if (isDeleted) {
            handleDeletedMessage(packageName, title, sbn.key)
        } else if (text.isNotBlank()) {
            saveMessage(packageName, title, bigText, sbn.key, sbn.postTime)
        }

        Log.d(TAG, "Notification from $packageName: $title - $text")
    }

    private fun saveMessage(
        packageName: String,
        sender: String,
        message: String,
        notificationKey: String,
        timestamp: Long
    ) {
        serviceScope.launch {
            try {
                val entity = MessageEntity(
                    packageName = packageName,
                    sender = sender,
                    message = message,
                    timestamp = timestamp,
                    notificationKey = notificationKey,
                    isDeleted = false
                )
                database.messageDao().insert(entity)
                Log.d(TAG, "Message saved: $sender - $message")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving message", e)
            }
        }
    }

    private fun handleDeletedMessage(packageName: String, sender: String, notificationKey: String) {
        serviceScope.launch {
            try {
                database.messageDao().markAsDeleted(sender, notificationKey)
                Log.d(TAG, "Message marked as deleted from $sender")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking message as deleted", e)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Optional
    }

    companion object {
        fun isEnabled(context: Context): Boolean {
            val packageName = context.packageName
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            return flat?.contains(packageName) == true
        }
    }
}