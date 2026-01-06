package com.wamr.recovery.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Update
    suspend fun update(message: MessageEntity)

    @Delete
    suspend fun delete(message: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getMessagesByApp(packageName: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE isDeleted = 1 ORDER BY timestamp DESC")
    fun getDeletedMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE sender = :sender ORDER BY timestamp DESC")
    fun getMessagesBySender(sender: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET isDeleted = 1 WHERE sender = :sender AND notificationKey = :key")
    suspend fun markAsDeleted(sender: String, key: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()

    @Query("DELETE FROM messages WHERE timestamp < :cutoffTime")
    suspend fun deleteOldMessages(cutoffTime: Long)

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun getMessageCount(): Int

    @Query("SELECT COUNT(*) FROM messages WHERE isDeleted = 1")
    suspend fun getDeletedMessageCount(): Int
}