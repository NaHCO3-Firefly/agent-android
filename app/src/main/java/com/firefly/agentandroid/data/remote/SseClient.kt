package com.firefly.agentandroid.data.remote

import com.firefly.agentandroid.data.remote.dto.ChatMessage
import com.firefly.agentandroid.data.remote.dto.ChatRequest
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request as OkHttpRequest
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class SseClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    fun streamChat(
        baseUrl: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessage>
    ): Flow<String> = callbackFlow {
        try {
            val request = ChatRequest(
                model = model,
                messages = messages,
                stream = true
            )

            val jsonBody = gson.toJson(request)
            val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

            val apiUrl = baseUrl.trimEnd('/') + "/chat/completions"

            val okHttpRequest = OkHttpRequest.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "text/event-stream")
                .post(requestBody)
                .build()

            val response = client.newCall(okHttpRequest).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                throw Exception("HTTP ${response.code}: $errorBody")
            }

            val body = response.body ?: throw Exception("Empty response body")
            val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))

            reader.use { br ->
                var line: String?
                while (br.readLine().also { line = it } != null) {
                    val currentLine = line ?: continue
                    if (currentLine.startsWith("data: ")) {
                        val data = currentLine.removePrefix("data: ")
                        if (data == "[DONE]") {
                            break
                        }
                        try {
                            val chatResponse = gson.fromJson(data, com.firefly.agentandroid.data.remote.dto.ChatResponse::class.java)
                            val content = chatResponse?.choices?.firstOrNull()?.delta?.content ?: ""
                            if (content.isNotEmpty()) {
                                trySend(content)
                            }
                        } catch (e: Exception) {
                            // 跳过解析失败的行
                        }
                    }
                }
            }

            response.close()
            close()
        } catch (e: Exception) {
            trySend("@error:${e.message}")
            close(e)
        }
        awaitClose {}
    }
}
