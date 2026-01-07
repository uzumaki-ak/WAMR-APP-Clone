package com.wamr.recovery.services

import android.app.Notification
import android.content.Context
import android.os.Environment
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
import java.io.File

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
            val mediaPath = detectMedia(bigText, packageName)
            saveMessage(packageName, title, bigText, sbn.key, sbn.postTime, mediaPath)
        }
    }

    private fun detectMedia(text: String, packageName: String): String? {
        val indicators = listOf("📷", "🎥", "🎵", "📄", "Photo", "Video", "Audio", "Document")

        if (indicators.any { text.contains(it) }) {
            return findLatestMedia(packageName)
        }
        return null
    }

    private fun findLatestMedia(packageName: String): String? {
        val basePath = "/Android/media/$packageName/WhatsApp/Media"
        val mediaFolders = listOf("WhatsApp Images", "WhatsApp Video", "WhatsApp Audio", "WhatsApp Documents")

        var latestFile: File? = null
        var latestTime = 0L

        val externalStorage = Environment.getExternalStorageDirectory()

        mediaFolders.forEach { folder ->
            val mediaDir = File(externalStorage, "$basePath/$folder")
            if (mediaDir.exists()) {
                mediaDir.listFiles()?.forEach { file ->
                    if (file.lastModified() > latestTime) {
                        latestTime = file.lastModified()
                        latestFile = file
                    }
                }
            }
        }

        return latestFile?.absolutePath
    }

    private fun saveMessage(
        packageName: String,
        sender: String,
        message: String,
        notificationKey: String,
        timestamp: Long,
        mediaPath: String?
    ) {
        serviceScope.launch {
            try {
                val entity = MessageEntity(
                    packageName = packageName,
                    sender = sender,
                    message = message,
                    timestamp = timestamp,
                    notificationKey = notificationKey,
                    isDeleted = false,
                    mediaPath = mediaPath,
                    mediaType = mediaPath?.let { detectMediaType(it) }
                )
                database.messageDao().insert(entity)
                Log.d(TAG, "Message saved: $sender - $message")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving message", e)
            }
        }
    }

    private fun detectMediaType(path: String): String {
        return when (File(path).extension.lowercase()) {
            "jpg", "jpeg", "png", "gif", "webp" -> "image"
            "mp4", "mkv", "avi" -> "video"
            "mp3", "wav", "ogg" -> "audio"
            else -> "document"
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

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

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