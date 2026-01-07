package com.wamr.recovery.models

data class ChatGroup(
    val sender: String,
    val messageCount: Int,
    val lastTimestamp: Long,
    val lastMessage: String,
    val chatName: String
)