package com.sarrawi.chat.uploadImage.up2

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://abdallah92.pythonanywhere.com/" // ضع رابط API الخاص بك هنا

    // إنشاء OkHttpClient مع زيادة المهلات
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // زيادة مهلة الاتصال
        .readTimeout(30, TimeUnit.SECONDS)    // زيادة مهلة القراءة
        .writeTimeout(30, TimeUnit.SECONDS)   // زيادة مهلة الكتابة
        .build()

    // إنشاء Retrofit باستخدام OkHttpClient
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // إضافة OkHttpClient هنا
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // دالة للحصول على Retrofit instance
    fun getInstance(): Retrofit {
        return retrofit
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}