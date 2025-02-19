package com.sarrawi.chat.notifications.entity

data class NotificationRequest(    val token: String,
                                   val message: String,
                                   val sender: String)
