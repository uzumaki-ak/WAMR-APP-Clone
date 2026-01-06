package com.wamr.recovery.models

data class MediaItem(
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val mediaType: MediaType,
    val timestamp: Long,
    val source: String
)

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    DOCUMENT,
    STATUS
}