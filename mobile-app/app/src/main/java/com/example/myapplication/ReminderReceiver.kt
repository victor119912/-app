package com.example.myapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "開賣提醒"
        val message = intent.getStringExtra("message") ?: "你有一個演唱會提醒"

        NotificationHelper.showNotification(context, title, message)
    }
}