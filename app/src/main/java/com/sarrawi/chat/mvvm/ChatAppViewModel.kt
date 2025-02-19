package com.sarrawi.chat.mvvm

import android.content.SharedPreferences
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
import com.sarrawi.chat.notifications.FirebaseService.Companion.token
import com.sarrawi.chat.notifications.entity.NotificationData
import com.sarrawi.chat.notifications.entity.PushNotification
import com.sarrawi.chat.notifications.entity.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import java.util.*
import kotlin.math.max
import kotlin.math.min
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bumptech.glide.Glide
import okhttp3.MediaType
import okhttp3.RequestBody

import com.google.firebase.firestore.Query
import com.sarrawi.chat.Utils.Companion.context
import com.sarrawi.chat.notifications.entity.NotificationRequest
import com.sarrawi.chat.uploadImage.ApiService
import com.sarrawi.chat.uploadImage.RetrofitClient
import com.sarrawi.chat.uploadImage.RetrofitClientInstance
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
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
                                  listenForNewMessages(context)
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








    fun listenForNewMessages(context: Context) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("Messages")
            .whereEqualTo("receiver_id", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                for (doc in snapshots!!.documentChanges) {
                    val senderId = doc.document.getString("sender_id") ?: continue
                    val messageText = doc.document.getString("message") ?: "رسالة جديدة"

                    showNotification(context, "رسالة جديدة", messageText)
                }
            }
    }

    fun showNotification(context: Context, title: String, message: String) {
        val channelId = "message_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Messages", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(1, notification)
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

//        firestore.collection("Conversation${Utils.getUidLoggedIn()}").document(friendid!!).update("person", "you")



    }


    private val apiService = RetrofitClient.getInstance().create(ApiService::class.java)



    // دالة لحفظ الرابط في Firestore
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
                            this@ChatAppViewModel.imageUrl.value = it // حفظ الرابط
                            updateProfile(it) // تمرير رابط الصورة إلى دالة تحديث البروفايل
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

    fun updateProfile(imageUrl: String) = viewModelScope.launch(Dispatchers.IO) {
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

        // تحديث بيانات المحادثات
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