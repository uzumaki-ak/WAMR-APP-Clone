package com.wamr.recovery.services

import android.content.Context
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import com.wamr.recovery.utils.FileUtils
import kotlinx.coroutines.*
import java.io.File

class StatusDownloader(private val context: Context) {

    private val TAG = "StatusDownloader"
    private var statusObserver: FileObserver? = null
    private val scanScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val processedFiles = mutableSetOf<String>()

    fun startMonitoring() {
        scanScope.launch {
            monitorStatusDirectory()
        }
    }

    fun stopMonitoring() {
        statusObserver?.stopWatching()
        statusObserver = null
        scanScope.cancel()
    }

    private fun monitorStatusDirectory() {
        val statusDir = getWhatsAppStatusDirectory()

        if (statusDir == null || !statusDir.exists()) {
            Log.w(TAG, "WhatsApp Status directory not found")
            return
        }

        val observer = object : FileObserver(statusDir, CREATE or MODIFY) {
            override fun onEvent(event: Int, path: String?) {
                if (path != null && !path.startsWith(".")) {
                    val file = File(statusDir, path)
                    if (file.isFile && !processedFiles.contains(file.absolutePath)) {
                        handleNewStatus(file)
                    }
                }
            }
        }

        observer.startWatching()
        statusObserver = observer
        Log.d(TAG, "Monitoring WhatsApp Status: ${statusDir.absolutePath}")

        scanExistingStatuses(statusDir)
    }

    private fun getWhatsAppStatusDirectory(): File? {
        val externalStorage = Environment.getExternalStorageDirectory()

        val possiblePaths = listOf(
            "WhatsApp/Media/.Statuses",
            "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"
        )

        for (path in possiblePaths) {
            val dir = File(externalStorage, path)
            if (dir.exists()) {
                return dir
            }
        }

        return null
    }

    private fun handleNewStatus(file: File) {
        scanScope.launch {
            try {
                delay(500)

                if (file.exists() && file.length() > 0 && isValidMediaFile(file)) {
                    val saved = FileUtils.copyStatusFile(context, file)
                    if (saved) {
                        processedFiles.add(file.absolutePath)
                        Log.d(TAG, "Status saved: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving status", e)
            }
        }
    }

    private fun scanExistingStatuses(directory: File) {
        scanScope.launch {
            directory.listFiles()?.forEach { file ->
                if (file.isFile && !file.name.startsWith(".") &&
                    !processedFiles.contains(file.absolutePath)) {
                    handleNewStatus(file)
                }
            }
        }
    }

    private fun isValidMediaFile(file: File): Boolean {
        val validExtensions = setOf("jpg", "jpeg", "png", "gif", "mp4", "webp")
        val extension = file.extension.lowercase()
        return extension in validExtensions && file.length() > 1024
    }
}