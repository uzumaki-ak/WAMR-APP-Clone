package com.wamr.recovery.models

data class Message(
    val id: Long = 0,
    val packageName: String,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val isDeleted: Boolean = false,
    val mediaPath: String? = null
)