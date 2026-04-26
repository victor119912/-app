package com.example.myapplication

data class ReminderItem(
    val id: Int,
    val title: String,
    val saleAt: String,
    val offsetsMinutes: String,
    val enabled: Boolean
)
