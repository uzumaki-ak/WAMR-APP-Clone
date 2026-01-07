package com.wamr.recovery.database

import androidx.room.*
import com.wamr.recovery.models.AppGroup
import com.wamr.recovery.models.ChatGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("""
        SELECT packageName, 
               COUNT(*) as messageCount,
               COUNT(DISTINCT sender) as chatCount,
               MAX(timestamp) as lastTimestamp,
               packageName as appName
        FROM messages 
        GROUP BY packageName 
        ORDER BY lastTimestamp DESC
    """)
    fun getAppsWithMessageCount(): Flow<List<AppGroup>>

    @Query("""
        SELECT sender,
               COUNT(*) as messageCount,
               MAX(timestamp) as lastTimestamp,
               (SELECT message FROM messages m2 
                WHERE m2.sender = messages.sender 
                AND m2.packageName = :packageName 
                ORDER BY timestamp DESC LIMIT 1) as lastMessage,
               sender as chatName
        FROM messages 
        WHERE packageName = :packageName 
        GROUP BY sender 
        ORDER BY lastTimestamp DESC
    """)
    fun getChatsWithMessageCount(packageName: String): Flow<List<ChatGroup>>

    @Query("""
        SELECT * FROM messages 
        WHERE sender = :sender AND packageName = :packageName 
        ORDER BY timestamp DESC
    """)
    fun getMessagesBySenderAndApp(sender: String, packageName: String): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE sender = :sender 
        AND packageName = :packageName 
        AND mediaPath IS NOT NULL 
        ORDER BY timestamp DESC
    """)
    fun getMediaMessagesBySender(sender: String, packageName: String): Flow<List<MessageEntity>>

    @Query("UPDATE messages SET isDeleted = 1 WHERE sender = :sender AND notificationKey = :key")
    suspend fun markAsDeleted(sender: String, key: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAll()
}