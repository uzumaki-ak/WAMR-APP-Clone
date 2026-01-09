package com.wamr.recovery.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MediaCopier(private val context: Context) {

    private val TAG = "WAMR_Copier"

    private fun getWAMRMediaFolder(): File {
        val wamrFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "WAMR_Recovery/Media"
        )
        if (!wamrFolder.exists()) {
            wamrFolder.mkdirs()
            Log.d(TAG, "📁 Created: ${wamrFolder.absolutePath}")
        }
        return wamrFolder
    }

    fun copyMediaToWAMRFolder(sourceFile: File): String? {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "📂 Copy request: ${sourceFile.name}")
        Log.d(TAG, "   Path: ${sourceFile.absolutePath}")
        Log.d(TAG, "   Exists: ${sourceFile.exists()}")
        Log.d(TAG, "   Can read: ${sourceFile.canRead()}")
        Log.d(TAG, "   Size: ${sourceFile.length()} bytes")

        if (!sourceFile.exists()) {
            Log.e(TAG, "❌ Source doesn't exist!")
            return null
        }

        if (!sourceFile.canRead()) {
            Log.e(TAG, "❌ Can't read source file!")
            return null
        }

        if (sourceFile.length() == 0L) {
            Log.e(TAG, "❌ Source file is empty!")
            return null
        }

        return try {
            val wamrFolder = getWAMRMediaFolder()
            val timestamp = System.currentTimeMillis()
            val extension = sourceFile.extension
            val destFile = File(wamrFolder, "WAMR_${timestamp}.$extension")

            Log.d(TAG, "📝 Copying to: ${destFile.name}")

            var bytesCopied = 0L
            FileInputStream(sourceFile).use { input ->
                FileOutputStream(destFile).use { output ->
                    bytesCopied = input.copyTo(output)
                }
            }

            Log.d(TAG, "✅ Copied $bytesCopied bytes")

            if (destFile.exists() && destFile.length() > 0) {
                Log.d(TAG, "✅ SUCCESS: ${destFile.absolutePath}")
                destFile.absolutePath
            } else {
                Log.e(TAG, "❌ Destination file invalid after copy")
                null
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "❌ PERMISSION ERROR: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "❌ COPY FAILED: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}