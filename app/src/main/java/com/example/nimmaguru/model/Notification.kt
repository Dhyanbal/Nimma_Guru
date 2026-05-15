package com.example.nimmaguru.model

data class Notification(
    val id: String = "",
    val receiverId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)