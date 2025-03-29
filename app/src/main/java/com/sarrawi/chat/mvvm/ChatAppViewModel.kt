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
import com.google.firebase.firestore.SetOptions

import kotlinx.coroutines.*

import java.util.*



import com.sarrawi.chat.notifications.entity.NotificationRequest

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream

import java.io.File

class ChatAppViewModel : ViewModel() {


    val message = MutableLiveData<String>()
    val firestore = FirebaseFirestore.getInstance()
    val name = MutableLiveData<String>()
    val imageUrl = MutableLiveData<String>()


    val usersRepo = UsersRepo()
    val messageRepo = MessageRepo()
    var token: String? = null
    val chatlistRepo = ChatListRepo()

    // دالة لتحديث الرابط




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


    fun sendMessage(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO) {

            val context = MyApplication.instance.applicationContext
            val messageText = message.value ?: return@launch // تجنب الأخطاء إذا كانت القيمة `null`
            if (messageText.isEmpty()) {
                Log.e("sendMessage", "Message text is empty")
                return@launch // تجنب إرسال رسالة فارغة
            }

            val time = Utils.getTime()
            val date = Utils.getCurrentDate()

            // إنشاء uniqueId باستخدام sender و receiver
            val uniqueId = listOf(sender, receiver).sorted().joinToString("")

            // تقسيم الاسم الأول بأمان
            val friendNameSplit = friendname.split("\\s+".toRegex()).firstOrNull() ?: friendname

            // تخزين بيانات المحادثة في Shared Preferences
            val mySharedPrefs = SharedPrefs(context).apply {
                setValue("friendid", receiver)
                setValue("chatroomid", uniqueId)
                setValue("friendname", friendNameSplit)
                setValue("friendimage", friendimage)
            }

            // تجهيز البيانات لإرسالها إلى Firestore
            val messageData = hashMapOf(
                "sender" to sender,
                "receiver" to receiver,
                "message" to messageText,
                "time" to time,
                "date" to date
            )

            // إرسال الرسالة إلى Firestore
            firestore.collection("Messages").document(uniqueId).collection("chats")
                .document(time).set(messageData)
                .addOnCompleteListener { taskMessage ->
                    if (taskMessage.isSuccessful) {
                        Log.d("Firestore", "Message sent successfully")

                        // تحديث قائمة المحادثات للطرفين
                        val senderChatData = hashMapOf(
                            "friendid" to receiver,
                            "time" to time,
                            "sender" to Utils.getUidLoggedIn(),
                            "message" to messageText,
                            "friendsimage" to friendimage, // ✅ صورة المستقبل هي صورة المستقبل
                            "name" to friendname,
                            "person" to "you"
                        )

                        val receiverChatData = hashMapOf(
                            "friendid" to sender,
                            "time" to time,
                            "message" to messageText,
                            "person" to (mySharedPrefs.getValue("username")?.substringBefore(" ") ?: sender),
                            "friendsimage" to friendimage, // ✅ استخدم صورة المرسل بدلاً من myprofileimage
                            "name" to (mySharedPrefs.getValue("username") ?: "")
                        )

                        firestore.collection("Conversation${Utils.getUidLoggedIn()}")
                            .document(receiver).set(senderChatData, SetOptions.merge())

                        firestore.collection("Conversation${receiver}")
                            .document(Utils.getUidLoggedIn()).set(receiverChatData, SetOptions.merge())

                        // التحقق من وجود توكن الإشعارات للطرف المستقبل
                        firestore.collection("Tokens").document(receiver)
                            .get()
                            .addOnSuccessListener { value ->
                                val token = value.toObject(Token::class.java)?.token
                                if (!token.isNullOrEmpty() && messageText.isNotEmpty() && receiver.isNotEmpty()) {
                                    val loggedInUsername = mySharedPrefs.getValue("username")
                                        ?.split("\\s+".toRegex())?.firstOrNull() ?: "User"

                                    // إرسال الإشعار
                                    // PushNotification(
                                    //     NotificationData(loggedInUsername, messageText), token
                                    // ).also {
                                    //     sendNotification(it)
                                    // }
                                } else {
                                    Log.e("ChatAppViewModel", "No token found, no notification sent")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("ChatAppViewModel", "Failed to retrieve token", e)
                            }

                        // إعادة تعيين الرسالة
                        message.postValue("")
                    } else {
                        Log.e("Firestore", "Failed to send message", taskMessage.exception)
                    }
                }
        }

    fun sendMessageeeeeeeee(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO) {

            val context = MyApplication.instance.applicationContext
            val messageText = message.value ?: return@launch // تجنب الأخطاء إذا كانت القيمة `null`
            if (messageText.isEmpty()) {
                Log.e("sendMessage", "Message text is empty")
                return@launch // تجنب إرسال رسالة فارغة
            }

            val time = Utils.getTime()
            val date = Utils.getCurrentDate() // الحصول على التاريخ الحالي

            // إنشاء uniqueId باستخدام sender و receiver
            val uniqueId = listOf(sender, receiver).sorted().joinToString("")

            // تقسيم الاسم الأول بأمان
            val friendNameSplit = friendname.split("\\s+".toRegex()).firstOrNull() ?: friendname

            // تخزين بيانات المحادثة في Shared Preferences
            val mySharedPrefs = SharedPrefs(context).apply {
                setValue("friendid", receiver)
                setValue("chatroomid", uniqueId)
                setValue("friendname", friendNameSplit)
                setValue("friendimage", friendimage)
            }

            // تجهيز البيانات لإرسالها إلى Firestore
            val messageData = hashMapOf(
                "sender" to sender,
                "receiver" to receiver,
                "message" to messageText,
                "time" to time,
                "date" to date
            )

            // إرسال الرسالة إلى Firestore
            firestore.collection("Messages").document(uniqueId).collection("chats")
                .document(time).set(messageData)
                .addOnCompleteListener { taskMessage ->

                    if (taskMessage.isSuccessful) {
                        // تحديث قائمة المحادثات للطرفين
                        val senderChatData = hashMapOf(
                            "friendid" to receiver,
                            "time" to time,
                            "sender" to Utils.getUidLoggedIn(),
                            "message" to messageText,
                            "friendsimage" to friendimage,
                            "name" to friendname,
                            "person" to "you"
                        )

                        val receiverChatData = hashMapOf(
                            "friendid" to sender,
                            "time" to time,
                            "message" to messageText,
                            "person" to (mySharedPrefs.getValue("username")?.substringBefore(" ") ?: sender),

                            "friendsimage" to (mySharedPrefs.getValue("myprofileimage") ?: ""), // ✅ تخزين صورة المرسل

//                            "friendsimage" to (mySharedPrefs.getValue("friendsimage") ?: ""), // صورة المرسل يتم تخزينها هنا

                            "name" to (mySharedPrefs.getValue("username") ?: "") // ✅ تخزين اسم المرسل
                        )

                        firestore.collection("Conversation${Utils.getUidLoggedIn()}")
                            .document(receiver).set(senderChatData, SetOptions.merge())

                        firestore.collection("Conversation${receiver}")
                            .document(Utils.getUidLoggedIn()).set(receiverChatData, SetOptions.merge())

                        // التحقق من وجود توكن الإشعارات للطرف المستقبل
                        firestore.collection("Tokens").document(receiver)
                            .get()
                            .addOnSuccessListener { value ->
                                val token = value.toObject(Token::class.java)?.token
                                if (!token.isNullOrEmpty() && messageText.isNotEmpty() && receiver.isNotEmpty()) {
                                    val loggedInUsername = mySharedPrefs.getValue("username")
                                        ?.split("\\s+".toRegex())?.firstOrNull() ?: "User"

                                    // إرسال الإشعار
                                    // PushNotification(
                                    //     NotificationData(loggedInUsername, messageText), token
                                    // ).also {
                                    //     sendNotification(it)
                                    // }
                                } else {
                                    Log.e("ChatAppViewModel", "No token found, no notification sent")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("ChatAppViewModel", "Failed to retrieve token", e)
                            }

                        // إعادة تعيين الرسالة
                        message.postValue("")
                    } else {
                        Log.e("Firestore", "Failed to send message", taskMessage.exception)
                    }
                }
        }

    fun sendMess11age(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO) {

            val context = MyApplication.instance.applicationContext
            val messageText = message.value ?: return@launch // تجنب الأخطاء إذا كانت القيمة `null`
            val time = Utils.getTime()
            val date = Utils.getCurrentDate() // الحصول على التاريخ الحالي

            // إنشاء uniqueId باستخدام sender و receiver
            val uniqueId = listOf(sender, receiver).sorted().joinToString("")

            // تقسيم الاسم الأول بأمان
            val friendNameSplit = friendname.split("\\s+".toRegex()).firstOrNull() ?: friendname

            // تخزين بيانات المحادثة في Shared Preferences
            val mySharedPrefs = SharedPrefs(context).apply {
                setValue("friendid", receiver)
                setValue("chatroomid", uniqueId)
                setValue("friendname", friendNameSplit)
                setValue("friendimage", friendimage)
            }

            // تجهيز البيانات لإرسالها إلى Firestore
            val messageData = hashMapOf(
                "sender" to sender,
                "receiver" to receiver,
                "message" to messageText,
                "time" to time,
                "date" to date
            )

            // إرسال الرسالة إلى Firestore
            firestore.collection("Messages").document(uniqueId).collection("chats")
                .document(time).set(messageData)
                .addOnCompleteListener { taskMessage ->

                    if (taskMessage.isSuccessful) {
                        // تحديث قائمة المحادثات للطرفين
                        val senderChatData = hashMapOf(
                            "friendid" to receiver,
                            "time" to time,
                            "sender" to Utils.getUidLoggedIn(),
                            "message" to messageText,
                            "friendsimage" to friendimage,
                            "name" to friendname,
                            "person" to "you"
                        )

                        val receiverChatData = hashMapOf(
                            "friendid" to sender,
                            "time" to time,
                            "message" to messageText,
                            "person" to (mySharedPrefs.getValue("username")?.substringBefore(" ") ?: sender),
                            "friendsimage" to (mySharedPrefs.getValue("myprofileimage") ?: ""), // ✅ تخزين صورة المرسل
                            "friendsimage" to (mySharedPrefs.getValue("friendsimage") ?: ""), // صورة المرسل يتم تخزينها هنا

                            "name" to (mySharedPrefs.getValue("username") ?: "") // ✅ تخزين اسم المرسل
                        )


//                        val receiverChatData = hashMapOf(
//                            "friendid" to sender,
//                            "time" to time,
//                            "message" to messageText,
//                            "person" to (mySharedPrefs.getValue("username")?.substringBefore(" ") ?: sender)
//
////                            "person" to mySharedPrefs.getValue("username")?.split("\\s+".toRegex())?.firstOrNull() ?: sender
//                        )

                        firestore.collection("Conversation${Utils.getUidLoggedIn()}")
                            .document(receiver).set(senderChatData, SetOptions.merge())

                        firestore.collection("Conversation${receiver}")
                            .document(Utils.getUidLoggedIn()).set(receiverChatData, SetOptions.merge())

                        // التحقق من وجود توكن الإشعارات للطرف المستقبل
                        firestore.collection("Tokens").document(receiver)
                            .get()
                            .addOnSuccessListener { value ->
                                val token = value.toObject(Token::class.java)?.token
                                if (!token.isNullOrEmpty() && messageText.isNotEmpty() && receiver.isNotEmpty()) {
                                    val loggedInUsername = mySharedPrefs.getValue("username")
                                        ?.split("\\s+".toRegex())?.firstOrNull() ?: "User"

                                    // إرسال الإشعار
                                    // PushNotification(
                                    //     NotificationData(loggedInUsername, messageText), token
                                    // ).also {
                                    //     sendNotification(it)
                                    // }
                                } else {
                                    Log.e("ChatAppViewModel", "No token found, no notification sent")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("ChatAppViewModel", "Failed to retrieve token", e)
                            }

                        // إعادة تعيين الرسالة
                        message.postValue("")
                    } else {
                        Log.e("Firestore", "Failed to send message", taskMessage.exception)
                    }
                }
        }

    fun sendMeسssage(sender: String, receiver: String, friendname: String, friendimage: String) =
        viewModelScope.launch(Dispatchers.IO) {

        val context = MyApplication.instance.applicationContext

        // تجهيز البيانات لإرسالها
        val messageText = message.value!!
        val time = Utils.getTime()
        val date = Utils.getCurrentDate()  // الحصول على التاريخ الحالي

        // البيانات التي سيتم تخزينها في Firestore
        val hashMap = hashMapOf<String, Any>(
            "sender" to sender,
            "receiver" to receiver,
            "message" to messageText,
            "time" to time,
            "date" to date  // إضافة التاريخ
        )

        // إنشاء uniqueId باستخدام sender و receiver
        val uniqueId = listOf(sender, receiver).sorted()
        uniqueId.joinToString(separator = "")

        val friendnamesplit = friendname.split("\\s".toRegex())[0]
        val mysharedPrefs = SharedPrefs(context)
        mysharedPrefs.setValue("friendid", receiver)
        mysharedPrefs.setValue("chatroomid", uniqueId.toString())
        mysharedPrefs.setValue("friendname", friendnamesplit)
        mysharedPrefs.setValue("friendimage", friendimage)

        // إرسال الرسالة إلى Firestore
        firestore.collection("Messages").document(uniqueId.toString()).collection("chats")
            .document(time).set(hashMap).addOnCompleteListener { taskmessage ->

                val setHashap = hashMapOf<String, Any>(
                    "friendid" to receiver,
                    "time" to time,
                    "sender" to Utils.getUidLoggedIn(),
                    "message" to messageText,
                    "friendsimage" to friendimage,
                    "name" to friendname,
                    "person" to "you"
                )

                firestore.collection("Conversation${Utils.getUidLoggedIn()}").document(receiver)
                    .set(setHashap)

                firestore.collection("Conversation${receiver}").document(Utils.getUidLoggedIn())
                    .update(
                        "message", messageText,
                        "time", time,
                        "person", name.value!!
                    )

                firestore.collection("Tokens").document(receiver).addSnapshotListener { value, error ->

                    if (value != null && value.exists()) {
                        val tokenObject = value.toObject(Token::class.java)
                        token = tokenObject?.token!!

                        val loggedInUsername = mysharedPrefs.getValue("username")!!.split("\\s".toRegex())[0]

                        if (messageText.isNotEmpty() && receiver.isNotEmpty()) {
                            // إرسال إشعار إذا كان هناك رسالة غير فارغة
                            // PushNotification(
                            //     NotificationData(loggedInUsername, messageText), token!!
                            // ).also {
                            //     sendNotification(it)
                            // }
                        } else {
                            Log.e("ChatAppViewModel", "NO TOKEN, NO NOTIFICATION")
                        }
                    }

                    Log.e("ViewModel", token.toString())

                    if (taskmessage.isSuccessful) {
                        message.value = ""
                        message.postValue("")  // إعادة تعيين الإدخال

                    }
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

    fun setImageUrl(newUrl: String) {
        Log.d("ChatAppViewModel", "تم تحديث رابط الصورة إلى: $newUrl")  // تسجيل القيمة

        imageUrl.postValue(newUrl) // تحديث LiveData
    }

    fun updateProfile() = viewModelScope.launch(Dispatchers.IO) {
        val context = MyApplication.instance.applicationContext

        val imageUrlValue = imageUrl.value ?: return@launch  // إذا لم يكن هناك رابط، لا تتابع

        val hashMapUser = hashMapOf<String, Any>(
            "username" to (name.value ?: ""),
            "imageUrl" to imageUrlValue  // حفظ رابط الصورة
        )

        firestore.collection("Users").document(Utils.getUidLoggedIn()).update(hashMapUser)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "تم تحديث الملف الشخصي بنجاح!", Toast.LENGTH_SHORT).show()
                }
            }

        // تحديث صورة المستخدم في محادثات الأصدقاء
        val mysharedPrefs = SharedPrefs(context)
        val friendid = mysharedPrefs.getValue("friendid")

        val hashMapUpdate = hashMapOf<String, Any>(
            "friendsimage" to imageUrlValue,
            "name" to (name.value ?: ""),
            "person" to (name.value ?: "")
        )

        firestore.collection("Conversation${friendid}").document(Utils.getUidLoggedIn()).update(hashMapUpdate)

        firestore.collection("Conversation${Utils.getUidLoggedIn()}").document(friendid!!).update("person", "you")
    }


    fun up1dateProfile() = viewModelScope.launch(Dispatchers.IO) {

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



}