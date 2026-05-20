package com.firefly.agentandroid.ui.main

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firefly.agentandroid.App
import com.firefly.agentandroid.R
import com.firefly.agentandroid.ui.chat.ChatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ConversationAdapter
    private lateinit var searchView: SearchView
    private lateinit var fabNewChat: FloatingActionButton
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        initViews()
        observeData()

        val prefs = com.firefly.agentandroid.util.SharedPrefsManager(this)
        if (!prefs.isConfigured()) {
            android.widget.Toast.makeText(this, "请先在设置中配置 API 地址和密钥", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_conversations)
        searchView = findViewById(R.id.search_view)
        fabNewChat = findViewById(R.id.fab_new_chat)

        adapter = ConversationAdapter(
            onItemClick = { conversation ->
                startActivity(ChatActivity.newIntent(this, conversation.id))
            },
            onItemLongClick = { conversation ->
                showDeleteDialog(conversation)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        fabNewChat.setOnClickListener {
            viewModel.createNewConversation { conversationId ->
                startActivity(ChatActivity.newIntent(this, conversationId))
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText ?: ""
                if (query.isBlank()) {
                    viewModel.getAllConversations()
                } else {
                    viewModel.searchConversations(query)
                }
                return true
            }
        })
    }

    private fun observeData() {
        viewModel.conversations.observe(this) { conversations ->
            adapter.submitList(conversations)
            if (conversations.isEmpty()) {
                findViewById<View>(R.id.text_empty).visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.text_empty).visibility = View.GONE
            }
        }
    }

    private fun showDeleteDialog(conversation: com.firefly.agentandroid.data.local.entity.Conversation) {
        AlertDialog.Builder(this)
            .setTitle("删除对话")
            .setMessage("确定要删除「${conversation.title}」吗？此操作不可恢复。")
            .setPositiveButton("删除") { _, _ ->
                viewModel.deleteConversation(conversation)
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
