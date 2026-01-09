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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class NotificationListener : NotificationListenerService() {

    private val TAG = "WAMR_Notif"
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val mediaCopier by lazy { MediaCopier(this) }

    // CRITICAL: Track processed files to avoid duplicates
    private val processedFiles = mutableSetOf<String>()

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

        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📱 FROM: $title")
        Log.d(TAG, "💬 MSG: $text")

        val isDeleted = text.contains("This message was deleted", ignoreCase = true) ||
                text.contains("deleted this message", ignoreCase = true)

        if (isDeleted) {
            handleDeletedMessage(packageName, title, sbn.key)
        } else if (text.isNotBlank()) {
            serviceScope.launch {
                val copiedMediaPath = findAndCopyMedia(packageName, text)
                saveMessage(packageName, title, bigText, sbn.key, sbn.postTime, copiedMediaPath)
            }
        }
    }

    private suspend fun findAndCopyMedia(packageName: String, messageText: String): String? {
        val mediaIndicators = listOf("📷", "🎥", "🎵", "📄", "Photo", "Video", "Audio", "Document", "Sticker", "PTT")

        if (!mediaIndicators.any { messageText.contains(it, ignoreCase = true) }) {
            Log.d(TAG, "❌ No media")
            return null
        }

        Log.d(TAG, "🔍 Media detected! Searching...")

        // Wait for WhatsApp to write
        delay(2000)

        val notificationTime = System.currentTimeMillis()

        // Try 3 times
        for (attempt in 1..3) {
            Log.d(TAG, "🔄 Attempt $attempt")

            val file = findNewestUnprocessedFile(packageName, notificationTime)

            if (file != null) {
                Log.d(TAG, "✅ Found: ${file.name}")

                // Mark as processed IMMEDIATELY
                processedFiles.add(file.absolutePath)

                val copied = mediaCopier.copyMediaToWAMRFolder(file)
                if (copied != null) {
                    Log.d(TAG, "✅ Copied successfully")
                    return copied
                }
            }

            if (attempt < 3) delay(1500)
        }

        Log.e(TAG, "❌ No file found")
        return null
    }

    private fun findNewestUnprocessedFile(packageName: String, notificationTime: Long): File? {
        val basePath = "/storage/emulated/0/Android/media/$packageName/WhatsApp/Media"

        val folders = listOf(
            "$basePath/WhatsApp Images/Private",
            "$basePath/WhatsApp Images/Sent",
            "$basePath/WhatsApp Video/Private",
            "$basePath/WhatsApp Video/Sent",
            "$basePath/WhatsApp Audio/Private",
            "$basePath/WhatsApp Audio/Sent",
            "$basePath/WhatsApp Voice Notes",
            "$basePath/WhatsApp Documents/Private",
            "$basePath/WhatsApp Documents/Sent",
            "$basePath/WhatsApp Stickers"
        )

        var newestFile: File? = null
        var newestTime = notificationTime - 60000 // Only files from last 60 seconds

        folders.forEach { folderPath ->
            val dir = File(folderPath)

            if (!dir.exists() || !dir.canRead()) {
                return@forEach
            }

            try {
                val files = dir.listFiles() ?: return@forEach

                files.forEach { file ->
                    if (file.isFile && file.canRead()) {
                        val filePath = file.absolutePath
                        val fileTime = file.lastModified()

                        // CRITICAL CHECKS:
                        // 1. Not already processed
                        // 2. Created AFTER notification came (within 60s before)
                        // 3. Newer than current newest
                        if (!processedFiles.contains(filePath) &&
                            fileTime > newestTime &&
                            fileTime <= notificationTime + 5000) { // Allow 5s clock drift

                            newestTime = fileTime
                            newestFile = file

                            val age = (notificationTime - fileTime) / 1000
                            Log.d(TAG, "   🆕 Candidate: ${file.name} (${age}s ago)")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error in $folderPath: ${e.message}")
            }
        }

        // Clean old processed files (keep last 100)
        if (processedFiles.size > 100) {
            val toRemove = processedFiles.size - 100
            processedFiles.drop(toRemove)
        }

        return newestFile
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
                val mediaType = mediaPath?.let { detectMediaType(it) }

                val entity = MessageEntity(
                    packageName = packageName,
                    sender = sender,
                    message = message,
                    timestamp = timestamp,
                    notificationKey = notificationKey,
                    isDeleted = false,
                    mediaPath = mediaPath,
                    mediaType = mediaType
                )
                database.messageDao().insert(entity)

                Log.d(TAG, "💾 Saved for $sender, media=${mediaPath != null}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ DB: ${e.message}")
            }
        }
    }

    private fun detectMediaType(path: String): String {
        return when (File(path).extension.lowercase()) {
            "jpg", "jpeg", "png", "gif", "webp" -> "image"
            "mp4", "mkv", "avi", "3gp" -> "video"
            "mp3", "wav", "ogg", "opus", "m4a" -> "audio"
            "pdf", "doc", "docx", "xls", "xlsx" -> "document"
            else -> "file"
        }
    }

    private fun handleDeletedMessage(packageName: String, sender: String, notificationKey: String) {
        serviceScope.launch {
            try {
                database.messageDao().markAsDeleted(sender, notificationKey)
                Log.d(TAG, "🗑️ Deleted: $sender")
            } catch (e: Exception) {
                Log.e(TAG, "❌ ${e.message}")
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