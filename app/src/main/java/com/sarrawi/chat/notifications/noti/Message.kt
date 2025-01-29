package com.sarrawi.chat.notifications.noti

data class Message(
    val token: String,
    val notification: NotificationData,
    val data: Map<String, String>
)//