package com.sarrawi.chat.notifications.noti

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.SharedPreferences
import android.util.Log


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // إرسال التوكن إلى الخادم لتخزينه
        // يمكنك تنفيذ ذلك باستخدام Retrofit أو أي طريقة تناسب تطبيقك
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // معالجة الإشعارات الواردة
        remoteMessage.notification?.let {
            val title = it.title
            val body = it.body
            Log.d("FCM", "Notification: Title: $title, Body: $body")
        }
    }
}
