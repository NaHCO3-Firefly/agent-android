package com.firefly.agentandroid.data.repository

import androidx.lifecycle.LiveData
import com.firefly.agentandroid.App
import com.firefly.agentandroid.data.local.AppDatabase
import com.firefly.agentandroid.data.local.entity.Conversation
import com.firefly.agentandroid.data.local.entity.Message
import com.firefly.agentandroid.data.remote.ApiService
import com.firefly.agentandroid.data.remote.RetrofitClient
import com.firefly.agentandroid.data.remote.SseClient
import com.firefly.agentandroid.data.remote.dto.ChatMessage
import com.firefly.agentandroid.data.remote.dto.ChatRequest
import com.firefly.agentandroid.util.SharedPrefsManager
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val database: AppDatabase) {

    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val sseClient = SseClient()
    private val prefs by lazy { SharedPrefsManager(App.instance) }

    fun getAllConversations(): LiveData<List<Conversation>> =
        conversationDao.getAllConversations()

    fun searchConversations(query: String): LiveData<List<Conversation>> =
        conversationDao.searchConversations(query)

    suspend fun createConversation(title: String = "新对话"): Long {
        val conversation = Conversation(title = title)
        return conversationDao.insert(conversation)
    }

    suspend fun updateConversationTitle(id: Long, title: String) {
        conversationDao.updateTitle(id, title)
        conversationDao.updateTimestamp(id)
    }

    suspend fun deleteConversation(conversation: Conversation) {
        conversationDao.delete(conversation)
    }

    fun getMessages(conversationId: Long): LiveData<List<Message>> =
        messageDao.getMessagesByConversation(conversationId)

    suspend fun getMessagesSync(conversationId: Long): List<Message> =
        messageDao.getMessagesByConversationSync(conversationId)

    suspend fun insertMessage(conversation: Conversation, role: String, content: String) {
        val msg = Message(
            conversationId = conversation.id,
            role = role,
            content = content
        )
        messageDao.insert(msg)
        conversationDao.updateTimestamp(conversation.id)
    }

    suspend fun updateMessageContent(messageId: Long, content: String) {
        messageDao.updateContent(messageId, content)
    }

    suspend fun sendChatCompletion(conversationId: Long): Result<String> {
        val messages = messageDao.getMessagesByConversationSync(conversationId)
            .map { ChatMessage(role = it.role, content = it.content) }

        val request = ChatRequest(
            model = prefs.modelName,
            messages = messages,
            stream = false,
            maxTokens = prefs.maxTokens,
            temperature = prefs.temperature.toDouble()
        )

        val apiService: ApiService = RetrofitClient.getInstance()
            .createApiService(prefs.baseUrl)

        return try {
            val response = apiService.chatCompletion(
                authorization = "Bearer ${prefs.apiKey}",
                request = request
            )
            if (response.isSuccessful) {
                val content = response.body()?.choices?.firstOrNull()?.message?.content ?: ""
                Result.success(content)
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.errorBody()?.string() ?: "未知错误"}"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun streamChatCompletion(conversationId: Long): Flow<String> {
        val prefs = SharedPrefsManager(App.instance)
        return sseClient.streamChat(
            baseUrl = prefs.baseUrl,
            apiKey = prefs.apiKey,
            model = prefs.modelName,
            messages = emptyList() // messages will be built at call site
        )
    }

    fun streamChatWithMessages(messages: List<ChatMessage>): Flow<String> {
        val prefs = SharedPrefsManager(App.instance)
        return sseClient.streamChat(
            baseUrl = prefs.baseUrl,
            apiKey = prefs.apiKey,
            model = prefs.modelName,
            messages = messages
        )
    }

    suspend fun getConversation(id: Long): Conversation? =
        conversationDao.getConversationById(id)
}
