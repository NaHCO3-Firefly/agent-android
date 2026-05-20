package com.firefly.agentandroid.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firefly.agentandroid.App
import com.firefly.agentandroid.R
import com.firefly.agentandroid.data.local.entity.Message

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var inputEditText: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var viewModel: ChatViewModel

    private var conversationId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        conversationId = intent.getLongExtra(EXTRA_CONVERSATION_ID, -1)
        if (conversationId == -1L) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ChatViewModel(
                    App.instance.repository,
                    conversationId
                ) as T
            }
        })[ChatViewModel::class.java]

        initViews()
        observeData()
        loadConversationTitle()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_messages)
        inputEditText = findViewById(R.id.edit_input)
        btnSend = findViewById(R.id.btn_send)
        btnStop = findViewById(R.id.btn_stop)

        adapter = MessageAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = adapter

        btnSend.setOnClickListener { sendMessage() }
        btnStop.setOnClickListener { viewModel.stopStreaming() }
        btnStop.visibility = View.GONE
    }

    private fun observeData() {
        viewModel.messages.observe(this) { messages ->
            adapter.submitList(messages)
            recyclerView.post {
                recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            btnSend.isEnabled = !isLoading
            btnStop.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                inputEditText.isEnabled = true
            }
        }

        viewModel.errorEvent.observe(this) { errorMsg ->
            if (errorMsg.isNotBlank()) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadConversationTitle() {
        viewModel.conversationTitle.observe(this) { title ->
            supportActionBar?.title = title
        }
    }

    private fun sendMessage() {
        val content = inputEditText.text.toString().trim()
        if (content.isBlank()) return

        val prefs = com.firefly.agentandroid.util.SharedPrefsManager(this)
        if (!prefs.isConfigured()) {
            Toast.makeText(this, "请先在设置中配置 API 地址和密钥", Toast.LENGTH_LONG).show()
            return
        }

        inputEditText.text.clear()
        inputEditText.isEnabled = false
        viewModel.sendMessage(content)
    }

    companion object {
        private const val EXTRA_CONVERSATION_ID = "conversation_id"

        fun newIntent(context: Context, conversationId: Long): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_CONVERSATION_ID, conversationId)
            }
        }
    }
}
