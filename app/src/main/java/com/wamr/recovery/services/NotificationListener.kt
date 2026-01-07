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
import com.wamr.recovery.utils.MediaCopier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class NotificationListener : NotificationListenerService() {

    private val TAG = "NotificationListener"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val mediaCopier by lazy { MediaCopier(this) }

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
            serviceScope.launch {
                val copiedMediaPath = findAndCopyLatestMedia(packageName, text)
                saveMessage(packageName, title, bigText, sbn.key, sbn.postTime, copiedMediaPath)
            }
        }
    }

    private suspend fun findAndCopyLatestMedia(packageName: String, messageText: String): String? {
        val mediaIndicators = listOf("📷", "🎥", "🎵", "📄", "Photo", "Video", "Audio", "Document", "Sticker")

        if (!mediaIndicators.any { messageText.contains(it) }) {
            return null
        }

        val mediaFolders = listOf(
            "WhatsApp Images/Private",
            "WhatsApp Images/Sent",
            "WhatsApp Video/Private",
            "WhatsApp Video/Sent",
            "WhatsApp Audio/Private",
            "WhatsApp Audio/Sent",
            "WhatsApp Documents/Private",
            "WhatsApp Documents/Sent",
            "WhatsApp Stickers"
        )

        var latestFile: File? = null
        var latestTime = System.currentTimeMillis() - 30000 // Last 30 seconds (increased from 10)

        val basePath = "/Android/media/$packageName/WhatsApp/Media"
        val externalStorage = Environment.getExternalStorageDirectory()

        mediaFolders.forEach { folder ->
            val fullPath = "$basePath/$folder"
            val mediaDir = File(externalStorage, fullPath)

            Log.d(TAG, "Scanning: ${mediaDir.absolutePath}, exists: ${mediaDir.exists()}")

            if (mediaDir.exists() && mediaDir.isDirectory) {
                mediaDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.lastModified() > latestTime) {
                        latestTime = file.lastModified()
                        latestFile = file
                        Log.d(TAG, "Found file: ${file.name}, modified: ${file.lastModified()}")
                    }
                }
            }
        }

        return if (latestFile != null) {
            val copiedPath = mediaCopier.copyMediaToWAMRFolder(latestFile!!)
            Log.d(TAG, "Media copy result: original=${latestFile!!.absolutePath}, copied=$copiedPath")
            copiedPath
        } else {
            Log.w(TAG, "No media file found in any folder")
            null
        }
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
                Log.d(TAG, "Saving message - sender: $sender, message: $message, mediaPath: $mediaPath")
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
                Log.d(TAG, "Message saved: $sender - $message - Media: $mediaPath")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving message", e)
            }
        }
    }

    private fun detectMediaType(path: String): String {
        return when (File(path).extension.lowercase()) {
            "jpg", "jpeg", "png", "gif", "webp" -> "image"
            "mp4", "mkv", "avi", "3gp" -> "video"
            "mp3", "wav", "ogg", "opus" -> "audio"
            "pdf", "doc", "docx", "xls", "xlsx" -> "document"
            else -> "file"
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