package com.firefly.agentandroid.util

import android.content.Context
import android.content.SharedPreferences

enum class ProviderPreset(val key: String, val label: String, val defaultBaseUrl: String, val defaultModel: String) {
    OPENAI("openai", "OpenAI", "https://api.openai.com/v1", "gpt-3.5-turbo"),
    OPENCODE_GO("opencode_go", "OpenCode Go", "https://opencode.ai/zen/go/v1", "deepseek-v4-pro"),
    CUSTOM("custom", "自定义", "", "");

    companion object {
        fun fromKey(key: String): ProviderPreset =
            entries.find { it.key == key } ?: OPENAI
    }
}

class SharedPrefsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var providerPreset: ProviderPreset
        get() {
            val key = prefs.getString(KEY_PROVIDER_PRESET, ProviderPreset.OPENAI.key) ?: ProviderPreset.OPENAI.key
            return ProviderPreset.fromKey(key)
        }
        set(value) = prefs.edit().putString(KEY_PROVIDER_PRESET, value.key).apply()

    var baseUrl: String
        get() {
            val saved = prefs.getString(KEY_BASE_URL, null)
            return saved ?: providerPreset.defaultBaseUrl
        }
        set(value) = prefs.edit().putString(KEY_BASE_URL, value).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var modelName: String
        get() {
            val saved = prefs.getString(KEY_MODEL_NAME, null)
            return saved ?: providerPreset.defaultModel
        }
        set(value) = prefs.edit().putString(KEY_MODEL_NAME, value).apply()

    var maxTokens: Int
        get() = prefs.getInt(KEY_MAX_TOKENS, 4096)
        set(value) = prefs.edit().putInt(KEY_MAX_TOKENS, value).apply()

    var temperature: Float
        get() = prefs.getFloat(KEY_TEMPERATURE, 0.7f)
        set(value) = prefs.edit().putFloat(KEY_TEMPERATURE, value).apply()

    fun isConfigured(): Boolean = apiKey.isNotBlank() && baseUrl.isNotBlank()

    companion object {
        private const val PREFS_NAME = "agent_android_prefs"
        private const val KEY_PROVIDER_PRESET = "provider_preset"
        private const val KEY_BASE_URL = "base_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_MODEL_NAME = "model_name"
        private const val KEY_MAX_TOKENS = "max_tokens"
        private const val KEY_TEMPERATURE = "temperature"
    }
}
