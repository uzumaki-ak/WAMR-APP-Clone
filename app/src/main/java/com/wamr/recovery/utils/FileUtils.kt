package com.wamr.recovery.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object FileUtils {

    private const val TAG = "FileUtils"
    private const val APP_FOLDER = "WAMR_Recovery"

    fun getAppStorageDirectory(context: Context): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            APP_FOLDER
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getMediaDirectory(context: Context): File {
        val dir = File(getAppStorageDirectory(context), "Media")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getStatusDirectory(context: Context): File {
        val dir = File(getAppStorageDirectory(context), "Status")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun copyMediaFile(context: Context, sourceFile: File): Boolean {
        return try {
            val targetDir = getMediaDirectory(context)
            val targetFile = File(targetDir, generateUniqueFileName(sourceFile.name))

            if (copyFile(sourceFile, targetFile)) {
                Log.d(TAG, "Media copied: ${targetFile.name}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error copying media", e)
            false
        }
    }

    fun copyStatusFile(context: Context, sourceFile: File): Boolean {
        return try {
            val targetDir = getStatusDirectory(context)
            val targetFile = File(targetDir, generateUniqueFileName(sourceFile.name))

            if (copyFile(sourceFile, targetFile)) {
                Log.d(TAG, "Status copied: ${targetFile.name}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error copying status", e)
            false
        }
    }

    private fun copyFile(source: File, target: File): Boolean {
        if (!source.exists() || source.length() == 0L) {
            return false
        }

        return try {
            FileInputStream(source).use { input ->
                FileOutputStream(target).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "File copy failed", e)
            false
        }
    }

    private fun generateUniqueFileName(originalName: String): String {
        val timestamp = System.currentTimeMillis()
        val extension = originalName.substringAfterLast(".", "")
        val baseName = originalName.substringBeforeLast(".")

        return if (extension.isNotEmpty()) {
            "${baseName}_${timestamp}.$extension"
        } else {
            "${originalName}_${timestamp}"
        }
    }

    fun cleanOldFiles(context: Context, daysToKeep: Int = 7) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)

        listOf(getMediaDirectory(context), getStatusDirectory(context)).forEach { dir ->
            dir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    file.delete()
                    Log.d(TAG, "Deleted old file: ${file.name}")
                }
            }
        }
    }

    fun getStorageSize(context: Context): Long {
        var totalSize = 0L
        listOf(getMediaDirectory(context), getStatusDirectory(context)).forEach { dir ->
            dir.listFiles()?.forEach { file ->
                totalSize += file.length()
            }
        }
        return totalSize
    }
}