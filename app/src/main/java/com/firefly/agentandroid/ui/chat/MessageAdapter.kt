package com.firefly.agentandroid.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.firefly.agentandroid.R
import com.firefly.agentandroid.data.local.entity.Message
import com.firefly.agentandroid.util.MarkdownRenderer

class MessageAdapter : ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        private const val VIEW_TYPE_USER = 0
        private const val VIEW_TYPE_ASSISTANT = 1
        private const val VIEW_TYPE_SYSTEM = 2

        private val DiffCallback = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
                oldItem.content == newItem.content
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).role) {
            "user" -> VIEW_TYPE_USER
            "assistant" -> VIEW_TYPE_ASSISTANT
            else -> VIEW_TYPE_SYSTEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_USER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_user, parent, false)
                UserViewHolder(view)
            }
            VIEW_TYPE_ASSISTANT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_ai, parent, false)
                AssistantViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_system, parent, false)
                SystemViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is UserViewHolder -> holder.bind(message)
            is AssistantViewHolder -> holder.bind(message)
            is SystemViewHolder -> holder.bind(message)
        }
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.text_content)
        private val timeText: TextView = itemView.findViewById(R.id.text_time)

        fun bind(message: Message) {
            contentText.text = message.content
            timeText.text = formatTime(message.timestamp)
        }
    }

    class AssistantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.text_content)
        private val timeText: TextView = itemView.findViewById(R.id.text_time)

        fun bind(message: Message) {
            MarkdownRenderer.render(contentText, message.content)
            timeText.text = formatTime(message.timestamp)
        }
    }

    class SystemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val contentText: TextView = itemView.findViewById(R.id.text_content)

        fun bind(message: Message) {
            contentText.text = message.content
        }
    }

    companion object TimeFormatter {
        private val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())

        private fun formatTime(timestamp: Long): String {
            val now = java.util.Calendar.getInstance()
            val msgCal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
            return if (now.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR) &&
                now.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR)
            ) {
                timeFormat.format(java.util.Date(timestamp))
            } else {
                val fmt = java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault())
                fmt.format(java.util.Date(timestamp))
            }
        }
    }
}
