package com.sarrawi.chat.uploadImage

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
interface ApiService {


    @Multipart
//    @POST("upload-imagefirebase/")
    @POST("upload_imageaa/")
    fun sa(@Part image: MultipartBody.Part): Response<ImageUploadResponse>



}