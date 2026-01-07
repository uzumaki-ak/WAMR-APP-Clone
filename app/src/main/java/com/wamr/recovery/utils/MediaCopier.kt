package com.wamr.recovery.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MediaCopier(private val context: Context) {

    private val TAG = "MediaCopier"

    private fun getWAMRMediaFolder(): File {
        val wamrFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "WAMR_Recovery/Media"
        )
        if (!wamrFolder.exists()) {
            wamrFolder.mkdirs()
            Log.d(TAG, "Created WAMR folder: ${wamrFolder.absolutePath}")
        }
        return wamrFolder
    }

    fun copyMediaToWAMRFolder(sourceFile: File): String? {
        Log.d(TAG, "Copy request: ${sourceFile.absolutePath}")
        Log.d(TAG, "Source exists: ${sourceFile.exists()}, size: ${sourceFile.length()}")

        if (!sourceFile.exists()) {
            Log.e(TAG, "Source file does not exist!")
            return null
        }

        if (sourceFile.length() == 0L) {
            Log.e(TAG, "Source file is empty!")
            return null
        }

        return try {
            val wamrFolder = getWAMRMediaFolder()
            val timestamp = System.currentTimeMillis()
            val extension = sourceFile.extension
            val destFile = File(wamrFolder, "WAMR_${timestamp}.$extension")

            Log.d(TAG, "Copying to: ${destFile.absolutePath}")

            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    val bytes = input.copyTo(output)
                    Log.d(TAG, "Copied $bytes bytes")
                }
            }

            if (destFile.exists() && destFile.length() > 0) {
                Log.d(TAG, "✅ Copy successful: ${destFile.name}, size: ${destFile.length()}")
                destFile.absolutePath
            } else {
                Log.e(TAG, "❌ Copy failed - destination file invalid")
                null
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Copy exception: ${e.message}", e)
            null
        }
    }
}