package com.wamr.recovery.utils

import android.content.Context
import android.os.Environment
import com.wamr.recovery.models.StatusItem
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class StatusManager(private val context: Context) {

    private val statusPaths = listOf(
        "/Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
        "WhatsApp/Media/.Statuses"
    )

    fun getAllStatuses(): List<StatusItem> {
        val statuses = mutableListOf<StatusItem>()
        val externalStorage = Environment.getExternalStorageDirectory()

        statusPaths.forEach { path ->
            val statusDir = File(externalStorage, path)
            if (statusDir.exists()) {
                statusDir.listFiles()?.forEach { file ->
                    if (file.isFile && !file.name.startsWith(".")) {
                        statuses.add(
                            StatusItem(
                                filePath = file.absolutePath,
                                fileName = file.name,
                                timestamp = file.lastModified(),
                                isVideo = file.extension in listOf("mp4", "mkv", "avi")
                            )
                        )
                    }
                }
            }
        }

        return statuses.sortedByDescending { it.timestamp }
    }

    fun downloadStatus(sourcePath: String, callback: (Boolean) -> Unit) {
        try {
            val source = File(sourcePath)
            if (!source.exists()) {
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

            FileInputStream(source).use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }

            callback(true)
        } catch (e: Exception) {
            e.printStackTrace()
            callback(false)
        }
    }
}