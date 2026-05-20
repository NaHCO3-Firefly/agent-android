package com.firefly.agentandroid.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firefly.agentandroid.data.local.entity.Conversation
import com.firefly.agentandroid.data.local.entity.Message
import com.firefly.agentandroid.data.remote.dto.ChatMessage
import com.firefly.agentandroid.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository,
    private val conversationId: Long
) : ViewModel() {

    private val _messages = repository.getMessages(conversationId)
    val messages: LiveData<List<Message>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorEvent = MutableLiveData("")
    val errorEvent: LiveData<String> = _errorEvent

    private val _conversationTitle = MutableLiveData("")
    val conversationTitle: LiveData<String> = _conversationTitle

    private var streamingJob: Job? = null
    private var assistantMessageId: Long = -1

    init {
        loadConversationDetails()
    }

    private fun loadConversationDetails() {
        viewModelScope.launch {
            val conversation = repository.getConversation(conversationId)
            _conversationTitle.value = conversation?.title ?: "对话"
        }
    }

    fun sendMessage(content: String) {
        _isLoading.value = true
        _errorEvent.value = ""

        viewModelScope.launch {
            val conversation = repository.getConversation(conversationId) ?: return@launch

            // 自动更新对话标题（首次对话时用第一条消息作为标题）
            val msgCount = repository.getMessagesSync(conversationId).size
            if (msgCount == 0) {
                val title = if (content.length > 20) content.take(20) + "..." else content
                repository.updateConversationTitle(conversationId, title)
                _conversationTitle.value = title
            }

            // 保存用户消息
            repository.insertMessage(conversation, "user", content)

            // 保存空的助手消息
            val assistantMsg = Message(
                conversationId = conversationId,
                role = "assistant",
                content = ""
            )
            assistantMessageId = repository.insertMessage(assistantMsg)

            try {
                // 获取所有消息作为上下文
                val allMessages = repository.getMessagesSync(conversationId)
                    .filter { it.id != assistantMessageId } // 排除当前空消息
                    .map { ChatMessage(role = it.role, content = it.content) }

                // 开始流式请求
                streamingJob = viewModelScope.launch {
                    val fullContent = StringBuilder()
                    repository.streamChatWithMessages(allMessages)
                        .catch { e ->
                            _errorEvent.postValue("请求失败: ${e.message}")
                            fullContent.append("\n\n[请求失败: ${e.message}]")
                        }
                        .collect { chunk ->
                            if (chunk.startsWith("@error:")) {
                                _errorEvent.postValue(chunk.removePrefix("@error:"))
                                fullContent.append("\n\n[${chunk}]")
                            } else {
                                fullContent.append(chunk)
                                repository.updateMessageContent(assistantMessageId, fullContent.toString())
                            }
                        }
                }

                streamingJob?.join()
                _isLoading.postValue(false)
            } catch (e: Exception) {
                _errorEvent.postValue("发送失败: ${e.message}")
                _isLoading.postValue(false)
            }
        }
    }

    fun stopStreaming() {
        streamingJob?.cancel()
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        streamingJob?.cancel()
    }
}
