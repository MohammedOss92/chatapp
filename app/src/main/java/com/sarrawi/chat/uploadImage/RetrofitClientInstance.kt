package com.sarrawi.chat.uploadImage

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientInstance {
    private const val BASE_URL = "http://www.sarrawi.com/"



        private val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val api: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl("http://www.sarrawi.com/") // ضع عنوان السيرفر المحلي هنا
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)
        }
    }


