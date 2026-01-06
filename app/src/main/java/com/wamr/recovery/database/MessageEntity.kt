package com.wamr.recovery.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val packageName: String,
    val sender: String,
    val message: String,
    val timestamp: Long,
    val notificationKey: String,
    val isDeleted: Boolean = false,
    val mediaPath: String? = null,
    val mediaType: String? = null
)