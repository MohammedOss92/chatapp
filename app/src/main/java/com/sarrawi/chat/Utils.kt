package com.sarrawi.chat

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import com.sarrawi.chat.notifications.FirebaseService.Companion.token
import com.sarrawi.chat.notifications.entity.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class Utils {


    companion object {
        @SuppressLint("StaticFieldLeak")
        val context = MyApplication.instance.applicationContext
        @SuppressLint("StaticFieldLeak")
        val firestore = FirebaseFirestore.getInstance()





        private val auth = FirebaseAuth.getInstance()
        private var userid: String = ""
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_IMAGE_PICK = 2
        const val MESSAGE_RIGHT = 1
        const val MESSAGE_LEFT = 2
        const val CHANNEL_ID = "com.sarrawi.chat"


        fun getUidLoggedIn(): String {

            if (auth.currentUser!=null){


                userid = auth.currentUser!!.uid



            }


            return userid



        }

        fun getTime(): String {


            val formatter = SimpleDateFormat("HH:mm:ss")
            val date: Date = Date(System.currentTimeMillis())
            val stringdate = formatter.format(date)


            return stringdate

        }

        fun getCurrentDate(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = Date(System.currentTimeMillis())
            return formatter.format(date)
        }

        fun formatDate(timestamp: String): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // استخراج التاريخ فقط
            val date = Date(timestamp.toLong()) // تحويل النص إلى رقم ثم إلى تاريخ
            return sdf.format(date)
        }

        // not using

//        fun getToken() : String{
//
//            var returntoken = ""
//
//            val mysharedPrefs = SharedPrefs(context)
//
//            val friendid = mysharedPrefs.getValue("friendid")
//
//
//            firestore.collection("Tokens").document(friendid!!).get().addOnSuccessListener {document->
//
//
//                if (document.exists() && document!=null){
//
//
//                    returntoken = document.toObject(Token::class.java).toString()
//
//
//
//
//
//                } else {
//
//                    Log.e("Utils", "TOKEN DOES NOT EXIST")
//                    Toast.makeText(context, "TOKEN DOES NOT EXISTS", Toast.LENGTH_SHORT).show()
//
//
//                }
//
//
//
//
//                Log.e("Utils", "Device token is ${returntoken}")
//
//
//
//
//
//            }
//
//
//
//
//
//            return returntoken
//
//        }
//



    }
}