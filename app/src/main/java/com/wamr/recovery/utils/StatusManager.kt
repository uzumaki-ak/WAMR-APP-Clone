package com.wamr.recovery.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.wamr.recovery.models.StatusItem
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class StatusManager(private val context: Context) {

    private val TAG = "StatusManager"

    private val statusPaths = listOf(
        "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses"
    )

    fun getAllStatuses(): List<StatusItem> {
        val statuses = mutableListOf<StatusItem>()
        val externalStorage = Environment.getExternalStorageDirectory()

        statusPaths.forEach { path ->
            val statusDir = File(externalStorage, path)
            Log.d(TAG, "Checking status dir: ${statusDir.absolutePath}, exists: ${statusDir.exists()}")

            if (statusDir.exists() && statusDir.isDirectory) {
                val files = statusDir.listFiles()
                Log.d(TAG, "Files in status dir: ${files?.size ?: 0}")

                files?.forEach { file ->
                    if (file.isFile && !file.name.startsWith(".") && file.name != ".nomedia") {
                        val isVideo = file.extension.lowercase() in listOf("mp4", "mkv", "avi", "3gp")
                        Log.d(TAG, "Found status: ${file.name}, isVideo: $isVideo")

                        statuses.add(
                            StatusItem(
                                filePath = file.absolutePath,
                                fileName = file.name,
                                timestamp = file.lastModified(),
                                isVideo = isVideo
                            )
                        )
                    }
                }
            } else {
                Log.w(TAG, "Status directory not found or not a directory")
            }
        }

        Log.d(TAG, "Total statuses found: ${statuses.size}")
        return statuses.sortedByDescending { it.timestamp }
    }

    fun downloadStatus(sourcePath: String, callback: (Boolean) -> Unit) {
        try {
            val source = File(sourcePath)
            if (!source.exists()) {
                Log.e(TAG, "Source file doesn't exist: $sourcePath")
                callback(false)
                return
            }

            val downloadDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "WAMR_Status"
            )
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }

            val dest = File(downloadDir, "Status_${System.currentTimeMillis()}_${source.name}")

            Log.d(TAG, "Downloading status: ${source.name} to ${dest.absolutePath}")

            FileInputStream(source).use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Status downloaded successfully")
            callback(true)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            callback(false)
        }
    }
}