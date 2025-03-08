package com.sarrawi.chat.mvvm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sarrawi.chat.Utils
import com.sarrawi.chat.adapter.RecentChatAdapter
import com.sarrawi.chat.modal.Messages
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.text.SimpleDateFormat
import java.util.*

class MessageRepo {

    val firestore = FirebaseFirestore.getInstance()




    fun gestMessages(friendid: String): LiveData<List<Messages>> {

        val messages = MutableLiveData<List<Messages>>()

        val uniqueId = listOf(Utils.getUidLoggedIn(), friendid).sorted()
        uniqueId.joinToString(separator = "")




        firestore.collection("Messages").document(uniqueId.toString()).collection("chats").orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {

                    return@addSnapshotListener
                }

                val messagesList = mutableListOf<Messages>()


                if (!snapshot!!.isEmpty) {


                    snapshot.documents.forEach { document ->

                        val messageModel = document.toObject(Messages::class.java)


                        if (messageModel!!.sender.equals(Utils.getUidLoggedIn()) && messageModel.receiver.equals(
                                friendid
                            ) ||
                            messageModel.sender.equals(friendid) && messageModel.receiver.equals(
                                Utils.getUidLoggedIn()
                            )
                        ) {
                            messageModel.let {


                                messagesList.add(it!!)


                            }
                        }

                    }



                    messages.value = messagesList

                }
            }

        return messages


    }



    fun getMessages(friendid: String): LiveData<List<Messages>> {

        val messages = MutableLiveData<List<Messages>>()

        val uniqueId = listOf(Utils.getUidLoggedIn(), friendid).sorted()
        uniqueId.joinToString(separator = "")

        FirebaseFirestore.getInstance().collection("Messages")
            .document(uniqueId.toString())
            .collection("chats")
            .orderBy("time", Query.Direction.ASCENDING)  // ترتيب حسب الوقت والتاريخ بشكل تصاعدي
            .addSnapshotListener { snapshot, exception ->

                if (exception != null) {
                    return@addSnapshotListener
                }

                val messagesList = mutableListOf<Messages>()

                if (!snapshot!!.isEmpty) {
                    snapshot.documents.forEach { document ->
                        val messageModel = document.toObject(Messages::class.java)

                        // استخراج الوقت كنص من الحقل time
                        val timestamp = messageModel!!.time

                        // إذا كان الوقت موجودًا، قم بتنسيقه
                        if (!timestamp.isNullOrEmpty()) {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            val formattedDate = dateFormat.format(Date()) // استخدام التاريخ الحالي لعرضه

                            messageModel.time = formattedDate // تعيين الوقت المنسق في الرسالة
                        }

                        // التحقق من المرسل والمستقبل
                        if (messageModel.sender.equals(Utils.getUidLoggedIn()) && messageModel.receiver.equals(friendid) ||
                            messageModel.sender.equals(friendid) && messageModel.receiver.equals(Utils.getUidLoggedIn())
                        ) {
                            messageModel.let {
                                messagesList.add(it!!)
                            }
                        }
                    }

                    // عكس ترتيب الرسائل بعد تحميلها إذا كنت تريد الأحدث في الأسفل
                    messagesList.reverse()  // عكس ترتيب الرسائل

                    messages.value = messagesList
                }
            }

        return messages
    }




}


