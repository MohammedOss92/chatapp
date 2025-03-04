package com.sarrawi.chat.uploadImage.up2


import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
interface ApiService {


    @Multipart
    @POST("upload_image/")
    fun sa(@Part image: MultipartBody.Part): Call<ImageUploadResponse>

//aa
    @Multipart
    @POST("api/upload/") // تأكد من المسار الصحيح
      fun uploadImage(
        @Part image: MultipartBody.Part
    ): Call<UploadResponse>

    @GET("api/upload/")
     fun getUploadedImages(): Call<List<UploadResponse>>

    companion object {
        operator fun invoke(): ApiService{
            return Retrofit.Builder()
                .baseUrl("https://abdallah92.pythonanywhere.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}