package com.firefly.agentandroid.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatResponse(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null
)

data class Choice(
    val index: Int? = null,
    val message: AssistantMessage? = null,
    val delta: DeltaMessage? = null,
    @SerializedName("finish_reason")
    val finishReason: String? = null
)

data class AssistantMessage(
    val role: String? = null,
    val content: String? = null
)

data class DeltaMessage(
    val role: String? = null,
    val content: String? = null
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int? = null,
    @SerializedName("completion_tokens")
    val completionTokens: Int? = null,
    @SerializedName("total_tokens")
    val totalTokens: Int? = null
)
