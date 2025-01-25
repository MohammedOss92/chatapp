package com.sarrawi.chat.notifications.noti
import FirebaseNotificationAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    val api: FirebaseNotificationAPI by lazy {
        Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirebaseNotificationAPI::class.java)
    }
}
