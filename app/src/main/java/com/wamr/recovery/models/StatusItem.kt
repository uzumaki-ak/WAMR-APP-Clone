package com.wamr.recovery.models

data class StatusItem(
    val filePath: String,
    val fileName: String,
    val timestamp: Long,
    val isVideo: Boolean
)