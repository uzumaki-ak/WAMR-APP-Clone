package com.wamr.recovery.services

import android.content.Context
import android.os.Environment
import android.os.FileObserver
import android.util.Log
import com.wamr.recovery.utils.FileUtils
import kotlinx.coroutines.*
import java.io.File

class MediaScanner(private val context: Context) {

    private val TAG = "MediaScanner"
    private var fileObserver: FileObserver? = null
    private val scanScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val whatsappMediaPaths = listOf(
        "WhatsApp/Media/.Statuses",
        "Android/media/com.whatsapp/WhatsApp/Media"
    )

    fun startMonitoring() {
        scanScope.launch {
            monitorWhatsAppMedia()
        }
    }

    fun stopMonitoring() {
        fileObserver?.stopWatching()
        fileObserver = null
        scanScope.cancel()
    }

    private fun monitorWhatsAppMedia() {
        val externalStorage = Environment.getExternalStorageDirectory()

        whatsappMediaPaths.forEach { relativePath ->
            val mediaDir = File(externalStorage, relativePath)
            if (mediaDir.exists() && mediaDir.isDirectory) {
                watchDirectory(mediaDir)
            } else {
                Log.w(TAG, "Media directory not found: ${mediaDir.absolutePath}")
            }
        }
    }

    private fun watchDirectory(directory: File) {
        val observer = object : FileObserver(directory, CREATE) {
            override fun onEvent(event: Int, path: String?) {
                if (event == CREATE && path != null) {
                    val newFile = File(directory, path)
                    if (newFile.isFile) {
                        handleNewMedia(newFile)
                    }
                }
            }
        }

        observer.startWatching()
        fileObserver = observer
        Log.d(TAG, "Monitoring directory: ${directory.absolutePath}")
    }

    private fun handleNewMedia(file: File) {
        scanScope.launch {
            try {
                delay(1000)

                if (file.exists() && file.length() > 0) {
                    val saved = FileUtils.copyMediaFile(context, file)
                    if (saved) {
                        Log.d(TAG, "Media saved: ${file.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling media file", e)
            }
        }
    }
}