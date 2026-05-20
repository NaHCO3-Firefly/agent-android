package com.firefly.agentandroid

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.firefly.agentandroid.data.local.AppDatabase
import com.firefly.agentandroid.data.repository.ChatRepository

class App : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
        repository = ChatRepository(database)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "消息通知",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "AI Agent 消息推送通知"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "agent_android_channel"
        lateinit var instance: App
            private set
    }
}
