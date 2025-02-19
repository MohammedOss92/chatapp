package com.sarrawi.chat.mvvm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sarrawi.chat.Utils
import com.sarrawi.chat.modal.Messages
import com.sarrawi.chat.modal.RecentChats
import com.sarrawi.chat.modal.Users
import com.sarrawi.chat.notifications.entity.Token
import com.google.firebase.firestore.FirebaseFirestore
import com.sarrawi.chat.uploadImage.RetrofitClientInstance
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UsersRepo {

    private val firestore = FirebaseFirestore.getInstance()


    fun getUsers(): LiveData<List<Users>> {

        val users = MutableLiveData<List<Users>>()

        firestore.collection("Users").addSnapshotListener { snapshot, exception ->

            if (exception != null) {

                return@addSnapshotListener
            }

            val usersList = mutableListOf<Users>()
            snapshot?.documents?.forEach { document ->

                val user = document.toObject(Users::class.java)

                if (user!!.userid != Utils.getUidLoggedIn()) {
                    user.let {


                        usersList.add(it)
                    }


                }


                users.value = usersList
            }


        }

        return users


    }


}