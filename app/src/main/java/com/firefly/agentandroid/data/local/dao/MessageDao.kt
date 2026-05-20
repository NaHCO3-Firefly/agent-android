package com.firefly.agentandroid.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.firefly.agentandroid.data.local.entity.Message

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesByConversation(conversationId: Long): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesByConversationSync(conversationId: Long): List<Message>

    @Insert
    suspend fun insert(message: Message): Long

    @Insert
    suspend fun insertAll(messages: List<Message>)

    @Update
    suspend fun update(message: Message)

    @Query("UPDATE messages SET content = :content WHERE id = :id")
    suspend fun updateContent(id: Long, content: String)

    @Delete
    suspend fun delete(message: Message)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteByConversation(conversationId: Long)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND id > :lastId ORDER BY timestamp ASC")
    suspend fun getMessagesAfter(conversationId: Long, lastId: Long): List<Message>
}
