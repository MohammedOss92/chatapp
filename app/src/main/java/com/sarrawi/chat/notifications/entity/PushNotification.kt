package com.sarrawi.chat.notifications.entity

data class PushNotification(
    val data: NotificationData,
    val to: String
)