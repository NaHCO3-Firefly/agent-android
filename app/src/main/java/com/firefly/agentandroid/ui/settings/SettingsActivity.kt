package com.firefly.agentandroid.ui.settings

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firefly.agentandroid.R
import com.firefly.agentandroid.util.ProviderPreset
import com.firefly.agentandroid.util.SharedPrefsManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPrefsManager
    private lateinit var spinnerProvider: Spinner
    private lateinit var editBaseUrl: EditText
    private lateinit var editApiKey: EditText
    private lateinit var editModelName: EditText
    private lateinit var editMaxTokens: EditText
    private lateinit var editTemperature: EditText
    private lateinit var btnSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = SharedPrefsManager(this)

        initViews()
        loadSettings()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initViews() {
        spinnerProvider = findViewById(R.id.spinner_provider)
        editBaseUrl = findViewById(R.id.edit_base_url)
        editApiKey = findViewById(R.id.edit_api_key)
        editModelName = findViewById(R.id.edit_model_name)
        editMaxTokens = findViewById(R.id.edit_max_tokens)
        editTemperature = findViewById(R.id.edit_temperature)
        btnSave = findViewById(R.id.btn_save)

        val presetLabels = ProviderPreset.entries.map { it.label }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, presetLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProvider.adapter = adapter

        spinnerProvider.setOnItemSelectedListener { _, _, position, _ ->
            val preset = ProviderPreset.entries[position]
            if (preset != ProviderPreset.CUSTOM) {
                editBaseUrl.setText(preset.defaultBaseUrl)
                editModelName.setText(preset.defaultModel)
                editBaseUrl.isEnabled = false
            } else {
                editBaseUrl.isEnabled = true
            }
        }

        btnSave.setOnClickListener { saveSettings() }
    }

    private fun loadSettings() {
        val currentPreset = prefs.providerPreset
        spinnerProvider.setSelection(currentPreset.ordinal)
        editBaseUrl.setText(prefs.baseUrl)
        editApiKey.setText(prefs.apiKey)
        editModelName.setText(prefs.modelName)
        editMaxTokens.setText(prefs.maxTokens.toString())
        editTemperature.setText(prefs.temperature.toString())
        editBaseUrl.isEnabled = currentPreset == ProviderPreset.CUSTOM
    }

    private fun saveSettings() {
        val position = spinnerProvider.selectedItemPosition
        val preset = ProviderPreset.entries.getOrElse(position) { ProviderPreset.CUSTOM }
        val baseUrl = editBaseUrl.text.toString().trim()
        val apiKey = editApiKey.text.toString().trim()
        val modelName = editModelName.text.toString().trim()
        val maxTokens = editMaxTokens.text.toString().trim().toIntOrNull() ?: 4096
        val temperature = editTemperature.text.toString().trim().toFloatOrNull() ?: 0.7f

        if (apiKey.isBlank()) {
            Toast.makeText(this, "API 密钥不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        if (preset == ProviderPreset.CUSTOM && baseUrl.isBlank()) {
            Toast.makeText(this, "API 地址不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        prefs.providerPreset = preset
        prefs.baseUrl = baseUrl
        prefs.apiKey = apiKey
        prefs.modelName = modelName
        prefs.maxTokens = maxTokens
        prefs.temperature = temperature

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
