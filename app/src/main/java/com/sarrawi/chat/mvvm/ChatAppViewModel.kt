package com.sarrawi.chat.mvvm

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sarrawi.chat.MyApplication
import com.sarrawi.chat.SharedPrefs
import com.sarrawi.chat.Utils
import com.sarrawi.chat.modal.Messages
import com.sarrawi.chat.modal.RecentChats
import com.sarrawi.chat.modal.Users
import com.sarrawi.chat.notifications.entity.NotificationDataa
import com.sarrawi.chat.notifications.entity.Token
import com.google.firebase.firestore.FirebaseFirestore
import com.sarrawi.chat.Utils.Companion.context
import com.sarrawi.chat.notifications.noti.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileInputStream

class ChatAppViewModel : ViewModel() {
    val message = MutableLiveData<String>()
    val firestore = FirebaseFirestore.getInstance()
    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()
    val usersRepo = UsersRepo()
    val messageRepo = MessageRepo()
    var token: String? = null
    val chatlistRepo = ChatListRepo()
    val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable -> throwable.printStackTrace() }

    init {
        getCurrentUser()
        getRecentUsers()
    }

    fun getUsers(): LiveData<List<Users>> {
        return usersRepo.getUsers()
    }




    // getting messages

    fun getMessages(friend: String): LiveData<List<Messages>> {

        return messageRepo.getMessages(friend)
    }


    // get RecentUsers


    fun getRecentUsers(): LiveData<List<RecentChats>> {


        return chatlistRepo.getAllChatList()

    }


    // دالة إرسال الرسالة
    fun sendMessage(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO) {  // تشغيل الكود داخل كوروتين باستخدام viewModelScope

            val context = MyApplication.instance.applicationContext

            // إعداد البيانات الخاصة بالرسالة
            val hashMap = hashMapOf<String, Any>(
                "sender" to sender,
                "receiver" to receiver,
                "message" to message.value!!,
                "time" to Utils.getTime()
            )

            val uniqueId = listOf(sender, receiver).sorted().joinToString(separator = "")

            val friendnamesplit = friendname.split("\\s".toRegex())[0]
            val mysharedPrefs = SharedPrefs(context)
            mysharedPrefs.setValue("friendid", receiver)
            mysharedPrefs.setValue("chatroomid", uniqueId)
            mysharedPrefs.setValue("friendname", friendnamesplit)
            mysharedPrefs.setValue("friendimage", friendimage)

            // إرسال الرسالة إلى Firestore
            firestore.collection("Messages").document(uniqueId).collection("chats")
                .document(Utils.getTime()).set(hashMap).addOnCompleteListener { taskMessage ->

                    if (taskMessage.isSuccessful) {
                        val setHashmap = hashMapOf<String, Any>(
                            "friendid" to receiver,
                            "time" to Utils.getTime(),
                            "sender" to Utils.getUidLoggedIn(),
                            "message" to message.value!!,
                            "friendsimage" to friendimage,
                            "name" to friendname,
                            "person" to "you"
                        )

                        firestore.collection("Conversation${Utils.getUidLoggedIn()}").document(receiver)
                            .set(setHashmap)

                        firestore.collection("Conversation${receiver}").document(Utils.getUidLoggedIn())
                            .update("message", message.value!!, "time", Utils.getTime(), "person", name.value!!)

                        // استماع إلى التوكن الخاص بالمستقبل (المستلم)
                        firestore.collection("Tokens").document(receiver).addSnapshotListener { value, _ ->
                            if (value != null && value.exists()) {
                                val tokenObject = value.toObject(Token::class.java)
                                val currentToken = tokenObject?.token

                                if (!currentToken.isNullOrEmpty()) {
                                    val loggedInUsername = mysharedPrefs.getValue("username")!!.split("\\s".toRegex())[0]

                                    if (message.value!!.isNotEmpty() && receiver.isNotEmpty()) {
                                        val notificationRequest = NotificationRequest(
                                            Message(currentToken, NotificationData(loggedInUsername, message.value!!), mapOf("senderId" to sender, "messageBody" to message.value!!))
                                        )

                                        // استدعاء الدالة لإرسال الإشعار عبر FCM
                                        viewModelScope.launch(Dispatchers.IO) {
//                                            sendFCMNotificationWithOAuth(notificationRequest)
                                        }

                                    } else {
                                        Log.e("ChatAppViewModel", "NO TOKEN, NO NOTIFICATION")
                                    }
                                } else {
                                    Log.e("ChatAppViewModel", "Token is null or empty")
                                }
                            }
                        }

                        if (taskMessage.isSuccessful) {
                            message.value = ""  // إعادة تعيين الرسالة بعد إرسالها بنجاح
                        }
                    }
                }
        }

//    private suspend fun sendFCMNotificationWithOAuth(notificationRequest: NotificationRequest) {
//        try {
//            // تحميل ملف حساب الخدمة (Service Account JSON)
////            val credentialsStream = context.assets.open("service.json")
////            val googleCredentials = GoogleCredentials.fromStream(credentialsStream)
////                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
////            googleCredentials.refreshIfExpired()
//
//            // الحصول على Access Token
////            val accessToken = googleCredentials.accessToken.tokenValue
//
//            // إعداد Retrofit أو استخدام OkHttp لإرسال الطلب
//            val client = OkHttpClient()
//            val mediaType = "application/json".toMediaType()
//
//            // JSON الخاص بالإشعار
//            val notificationJson = """
//            {
//              "to": "${notificationRequest.message.token}",
//              "notification": {
//                "title": "${notificationRequest.message.notification}",
//                "body": "${notificationRequest.message.data}"
//              }
//            }
//        """.trimIndent()
//
//            val requestBody = RequestBody.create(mediaType, notificationJson)
//
//            // إعداد طلب HTTP
//            val request = Request.Builder()
//                .url("https://fcm.googleapis.com/v1/projects/YOUR_PROJECT_ID/messages:send")
//                .post(requestBody)
//                .addHeader("Authorization", "Bearer $accessToken")
//                .addHeader("Content-Type", "application/json")
//                .build()
//
//            // تنفيذ الطلب
//            val response = client.newCall(request).execute()
//
//            if (response.isSuccessful) {
//                println("Notification sent successfully: ${response.body?.string()}")
//            } else {
//                println("Failed to send notification: ${response.body?.string()}")
//            }
//        } catch (e: Exception) {
//            println("Error sending notification: ${e.message}")
//        }
//    }


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


    fun updateProfilea() = viewModelScope.launch(Dispatchers.IO) {

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

    fun updateProfile() = viewModelScope.launch(Dispatchers.IO) {

        val context = MyApplication.instance.applicationContext

        // تحقق من أن القيمة الخاصة بالاسم ليست فارغة
        if (name.value.isNullOrEmpty()) {
            // إذا كانت القيمة فارغة، يمكن أن تعرض رسالة خطأ أو تحذير
            Log.e("ChatAppViewModel", "Name is null or empty.")
            return@launch // الخروج من الدالة إذا كانت القيمة غير صالحة
        }

        val hashMapUser = hashMapOf<String, Any>(
            "username" to name.value!!
        )

        firestore.collection("Users").document(Utils.getUidLoggedIn()).update(hashMapUser).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Updated", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("ChatAppViewModel", "Failed to update user profile.")
            }
        }

        val mysharedPrefs = SharedPrefs(context)
        val friendid = mysharedPrefs.getValue("friendid")

        // تحقق من أن friendid ليس null قبل استخدامه
        if (!friendid.isNullOrEmpty()) {
            val hashMapUpdate = hashMapOf<String, Any>(
                "name" to name.value!!,
                "person" to name.value!!
            )

            firestore.collection("Conversation${friendid}")
                .document(Utils.getUidLoggedIn()).update(hashMapUpdate)

            firestore.collection("Conversation${Utils.getUidLoggedIn()}")
                .document(friendid).update("person", "you")
        } else {
            Log.e("ChatAppViewModel", "Friend ID is null or empty!")
        }
    }



}