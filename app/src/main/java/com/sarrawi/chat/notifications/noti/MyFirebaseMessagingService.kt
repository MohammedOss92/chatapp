package com.sarrawi.chat.notifications.noti

import android.content.Context
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.SharedPreferences
import android.util.Log



class MyFirebaseMessagingService : FirebaseMessagingService() {

    // مفتاح الـ SharedPreferences
    private val sharedPreferencesName = "MyAppPreferences"
    private val tokenKey = "fcm_token"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")

        // حفظ التوكن في SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(tokenKey, token)
        editor.apply() // حفظ التغييرات

        // إرسال التوكن إلى الخادم إذا لزم الأمر
        // يمكنك استخدام Retrofit أو أي طريقة أخرى لتخزين التوكن على الخادم
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

    // دالة لقراءة التوكن المخزن من SharedPreferences
    fun getSavedToken(): String? {
        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE)
        return sharedPreferences.getString(tokenKey, null)
    }
}


/*
* val savedToken = myFirebaseMessagingService.getSavedToken()
if (savedToken != null) {
    // استخدم التوكن المحفوظ
} else {
    Log.e("FCM", "No token found.")
}
*/