package com.firefly.agentandroid.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    val temperature: Double = 0.7,
    @SerializedName("max_tokens")
    val maxTokens: Int = 4096
)

data class ChatMessage(
    val role: String,
    val content: String
)
