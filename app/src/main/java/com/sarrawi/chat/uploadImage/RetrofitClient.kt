package com.sarrawi.chat.uploadImage

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://www.sarrawi.com/" // ضع رابط API الخاص بك هنا

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun getInstance(): Retrofit {
        return retrofit
    }
}
