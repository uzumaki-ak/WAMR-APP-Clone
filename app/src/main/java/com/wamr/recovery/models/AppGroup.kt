package com.wamr.recovery.models

data class AppGroup(
    val packageName: String,
    val messageCount: Int,
    val chatCount: Int,
    val lastTimestamp: Long,
    val appName: String
)