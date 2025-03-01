package com.sarrawi.chat.mvvm

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarrawi.chat.MyApplication
import com.sarrawi.chat.R
import com.sarrawi.chat.SharedPrefs
import com.sarrawi.chat.Utils
import com.sarrawi.chat.modal.Messages
import com.sarrawi.chat.modal.RecentChats
import com.sarrawi.chat.modal.Users

import com.sarrawi.chat.notifications.entity.Token

import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.*

import java.util.*



import com.sarrawi.chat.notifications.entity.NotificationRequest
import com.sarrawi.chat.uploadImage.ApiService
import com.sarrawi.chat.uploadImage.Resource
import com.sarrawi.chat.uploadImage.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream

import java.io.File

class ChatAppViewModel : ViewModel() {

    private val _imageUrls = MutableLiveData<String>()
    val imageUrls: LiveData<String> get() = _imageUrls

    val message = MutableLiveData<String>()
    val firestore = FirebaseFirestore.getInstance()
    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()


    val usersRepo = UsersRepo()
    val messageRepo = MessageRepo()
    var token: String? = null
    val chatlistRepo = ChatListRepo()
    private val _imageUrl2 = MutableLiveData<String>()
    val imageUrl2: LiveData<String> get() = _imageUrl2
    private val _uploadResult = MutableLiveData<Resource<String>>()
    val uploadResult: LiveData<Resource<String>> get() = _uploadResult

    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }


    init {

        getCurrentUser()
        getRecentUsers()
    }

    fun getUsers(): LiveData<List<Users>> {
        return usersRepo.getUsers()


    }


    // sendMessage

    fun sendMessage(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO) {

            val context = MyApplication.instance.applicationContext

            val hashMap = hashMapOf<String, Any>(
                "sender" to sender,
                "receiver" to receiver,
                "message" to message.value!!,
                "time" to Utils.getTime()
            )


            val uniqueId = listOf(sender, receiver).sorted()
            uniqueId.joinToString(separator = "")


            val friendnamesplit = friendname.split("\\s".toRegex())[0]
            val mysharedPrefs = SharedPrefs(context)
            mysharedPrefs.setValue("friendid", receiver)
            mysharedPrefs.setValue("chatroomid", uniqueId.toString())
            mysharedPrefs.setValue("friendname", friendnamesplit)
            mysharedPrefs.setValue("friendimage", friendimage)




            firestore.collection("Messages").document(uniqueId.toString()).collection("chats")
                .document(Utils.getTime()).set(hashMap).addOnCompleteListener { taskmessage ->


                    val setHashap = hashMapOf<String, Any>(
                        "friendid" to receiver,
                        "time" to Utils.getTime(),
                        "sender" to Utils.getUidLoggedIn(),
                        "message" to message.value!!,
                        "friendsimage" to friendimage,
                        "name" to friendname,
                        "person" to "you"
                    )


                    firestore.collection("Conversation${Utils.getUidLoggedIn()}").document(receiver)
                        .set(setHashap)



                    firestore.collection("Conversation${receiver}").document(Utils.getUidLoggedIn())
                        .update(
                            "message",
                            message.value!!,
                            "time",
                            Utils.getTime(),
                            "person",
                            name.value!!
                        )



                      firestore.collection("Tokens").document(receiver).addSnapshotListener { value, error ->


                          if (value != null && value.exists()) {


                              val tokenObject = value.toObject(Token::class.java)


                              token = tokenObject?.token!!


                              val loggedInUsername =
                                  mysharedPrefs.getValue("username")!!.split("\\s".toRegex())[0]



                              if (message.value!!.isNotEmpty() && receiver.isNotEmpty()) {

                                  val notificationRequest = NotificationRequest(
                                      token!!,  // التوكن المستلم من Firestore
                                      message.value!!,  // الرسالة
                                      loggedInUsername  // اسم المرسل
                                  )
//                                      sendNotificationToDjango(notificationRequest)
                                  }

                              } else {


                                  Log.e("ChatAppViewModel", "NO TOKEN, NO NOTIFICATION")
                              }


                          }

                          Log.e("ViewModel", token.toString())



                          if (taskmessage.isSuccessful){

                              message.value = ""



                          }


                      }
                   }










    // getting messages

    fun getMessages(friend: String): LiveData<List<Messages>> {

        return messageRepo.getMessages(friend)
    }


    // get RecentUsers




    fun getRecentUsers(): LiveData<List<RecentChats>> {


        return chatlistRepo.getAllChatList()

    }






    fun getCurrentUser() = viewModelScope.launch(Dispatchers.IO) {

        val context = MyApplication.instance.applicationContext


        firestore.collection("Users").document(Utils.getUidLoggedIn())
            .addSnapshotListener { value, error ->


                if (value!!.exists() && value != null) {

                    val users = value.toObject(Users::class.java)
                    name.value = users?.username!!
                    imageUrl.value = users.imageUrl!!


                    val mysharedPrefs = SharedPrefs(context)
                    mysharedPrefs.setValue("username", users.username!!)


                }


            }


    }





    private val apiService = RetrofitClient.getInstance().create(ApiService::class.java)



    // دالة لحفظ الرابط في Firestore



    fun updateProfile() = viewModelScope.launch(Dispatchers.IO) {

        val context = MyApplication.instance.applicationContext

        val hashMapUser =
            hashMapOf<String, Any>("username" to name.value!!, "imageUrl" to imageUrl.value!!)

        firestore.collection("Users").document(Utils.getUidLoggedIn()).update(hashMapUser).addOnCompleteListener {

            if (it.isSuccessful){

                Toast.makeText(context, "Updated", Toast.LENGTH_SHORT ).show()


            }

        }


        val mysharedPrefs = SharedPrefs(context)
        val friendid = mysharedPrefs.getValue("friendid")

        val hashMapUpdate = hashMapOf<String, Any>("friendsimage" to imageUrl.value!!, "name" to name.value!!, "person" to name.value!!)



        // updating the chatlist and recent list message, image etc

        firestore.collection("Conversation${friendid}").document(Utils.getUidLoggedIn()).update(hashMapUpdate)

        firestore.collection("Conversation${Utils.getUidLoggedIn()}").document(friendid!!).update("person", "you")



    }



    fun uploadImageToServer(imageFile: File, userId: String) {
        val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)

        viewModelScope.launch {
            _uploadResult.postValue(Resource.Loading())

            try {
                val response =messageRepo .uploadImage(imagePart)
                if (response.isSuccessful && response.body() != null) {
                    val imageUrl = response.body()!!.imageUrl

                    // تحديث Firestore برابط الصورة
                    firestore.collection("users").document(userId)
                        .update("profileImage", imageUrl)
                        .addOnSuccessListener {
                            _uploadResult.postValue(Resource.Success(imageUrl))
                        }
                        .addOnFailureListener {
                            _uploadResult.postValue(Resource.Error("Failed to update Firestore"))
                        }
                } else {
                    _uploadResult.postValue(Resource.Error("Upload failed"))
                }
            } catch (e: Exception) {
                _uploadResult.postValue(Resource.Error("Error: ${e.message}"))
            }
        }
    }


    //deep

    fun uploadImageAndUpdateProfile(imageBitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), data)
        val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.sa(imagePart)
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.imageUrl
                    imageUrl?.let {
                        withContext(Dispatchers.Main) {
                            _imageUrl2.value = it // تحديث LiveData
                            updateProfilea(it)   // تحديث البروفايل إذا لزم الأمر
                        }
                    }
                } else {
                    Log.w("Retrofit", "فشل رفع الصورة: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "حدث خطأ أثناء رفع الصورة: ${e.message}")
            }
        }
    }

    fun uploadImageAndUpdateProfile2(imageBitmap: Bitmap?) {
        val baos = ByteArrayOutputStream()
        imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val requestBody = RequestBody.create("image/jpeg".toMediaTypeOrNull(), data)
        val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.sa(imagePart)
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.imageUrl
                    imageUrl?.let {
                        withContext(Dispatchers.Main) {
                            this@ChatAppViewModel.imageUrl.value = it // حفظ الرابط
                            updateProfilea(it) // تمرير رابط الصورة إلى دالة تحديث البروفايل
                        }
                    }
                } else {
                    Log.w("Retrofit", "فشل رفع الصورة: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("Retrofit", "حدث خطأ أثناء رفع الصورة: ${e.message}")
            }
        }
    }

    fun updateProfilea(imageUrl: String) = viewModelScope.launch(Dispatchers.IO) {
        val context = MyApplication.instance.applicationContext

        val hashMapUser = hashMapOf<String, Any>(
            "username" to (name.value ?: ""),
            "imageUrl" to imageUrl
        )

        firestore.collection("Users").document(Utils.getUidLoggedIn())
            .update(hashMapUser)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "تم تحديث الملف الشخصي بنجاح", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "فشل تحديث الملف الشخصي", Toast.LENGTH_SHORT).show()
                }
            }

        val mySharedPrefs = SharedPrefs(context)
        val friendId = mySharedPrefs.getValue("friendid")

        val hashMapUpdate = hashMapOf<String, Any>(
            "friendsimage" to imageUrl,
            "name" to (name.value ?: ""),
            "person" to (name.value ?: "")
        )

        firestore.collection("Conversation${friendId}")
            .document(Utils.getUidLoggedIn())
            .update(hashMapUpdate)

        firestore.collection("Conversation${Utils.getUidLoggedIn()}")
            .document(friendId ?: "")
            .update("person", "you")
    }

}