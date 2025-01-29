package com.sarrawi.chat.notifications.noti

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface FCMApi {
    @POST("v1/projects/chat-977a1/messages:send")
    suspend fun sendNotification(
        @Header("Authorization") authorization: String,
        @Body notificationRequest: NotificationRequest
    ): Response<ResponseBody>
}
