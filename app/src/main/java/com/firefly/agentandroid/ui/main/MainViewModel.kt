package com.firefly.agentandroid.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.firefly.agentandroid.App
import com.firefly.agentandroid.data.local.entity.Conversation
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val repository = App.instance.repository

    private val _conversations = repository.getAllConversations()

    val conversations: LiveData<List<Conversation>> = _conversations

    fun getAllConversations() {
        // LiveData auto-updates via Room
    }

    fun searchConversations(query: String) {
        // Trigger re-observation from caller via searchConversations
    }

    fun searchConversations(query: String): LiveData<List<Conversation>> {
        return repository.searchConversations(query)
    }

    fun createNewConversation(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createConversation()
            onCreated(id)
        }
    }

    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            repository.deleteConversation(conversation)
        }
    }
}
