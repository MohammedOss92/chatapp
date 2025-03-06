@file:Suppress("DEPRECATION")

package com.sarrawi.chat.fragments

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sarrawi.chat.R
import com.sarrawi.chat.SharedPrefs
import com.sarrawi.chat.Utils
import com.sarrawi.chat.activities.SignInActivity
import com.sarrawi.chat.adapter.*
import com.sarrawi.chat.databinding.FragmentHomeBinding
import com.sarrawi.chat.modal.RecentChats
import com.sarrawi.chat.modal.Users
import com.sarrawi.chat.mvvm.ChatAppViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import de.hdodenhof.circleimageview.CircleImageView

class HomeFragment : Fragment(), OnItemClickListener, onChatClicked {


    lateinit var rvUsers : RecyclerView
    lateinit var rvRecentChats : RecyclerView
    lateinit var adapter : UserAdapter
    lateinit var viewModel : ChatAppViewModel
    lateinit var toolbar: Toolbar
    lateinit var circleImageView: CircleImageView
    lateinit var recentadapter : RecentChatAdapter
    lateinit var firestore : FirebaseFirestore
    lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this).get(ChatAppViewModel::class.java)
        toolbar = view.findViewById(R.id.toolbarMain)
        val logoutimage = toolbar.findViewById<ImageView>(R.id.logOut)
        circleImageView = toolbar.findViewById(R.id.tlImage)



        binding.lifecycleOwner = viewLifecycleOwner



        viewModel.imageUrl.observe(viewLifecycleOwner, Observer {


            Glide.with(requireContext()).load(it).into(circleImageView)





        })

        listenForNewMessages(requireContext())



//
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
//            object : OnBackPressedCallback(true) {
//                override fun handleOnBackPressed() {
//                    getActivity()?.moveTaskToBack(true);
//                    getActivity()?.finish();
//
//                }
//
//            })

        firestore = FirebaseFirestore.getInstance()


        val firebaseAuth = FirebaseAuth.getInstance()



        logoutimage.setOnClickListener {


            firebaseAuth.signOut()

            startActivity(Intent(requireContext(), SignInActivity::class.java))


        }


        rvUsers = view.findViewById(R.id.rvUsers)
        rvRecentChats = view.findViewById(R.id.rvRecentChats)
        adapter = UserAdapter()
        recentadapter = RecentChatAdapter()


        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        val layoutManager2 = LinearLayoutManager(activity)

        rvUsers.layoutManager = layoutManager
        rvRecentChats.layoutManager= layoutManager2


        viewModel.getUsers().observe(viewLifecycleOwner, Observer {

            adapter.setList(it)
            rvUsers.adapter = adapter


        })


        circleImageView.setOnClickListener {


            view.findNavController().navigate(R.id.action_homeFragment_to_settingFragment)


        }


        adapter.setOnClickListener(this)





        viewModel.getRecentUsers().observe(viewLifecycleOwner, Observer {


            recentadapter.setList(it)
            rvRecentChats.adapter = recentadapter





        })

        recentadapter.setOnChatClickListener(this)






    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }


    override fun onUserSelected(position: Int, users: Users) {

        val action = HomeFragmentDirections.actionHomeFragmentToChatFragment(users)
        view?.findNavController()?.navigate(action)




    }


    override fun getOnChatCLickedItem(position: Int, chatList: RecentChats) {


        val action = HomeFragmentDirections.actionHomeFragmentToChatfromHome(chatList)
        view?.findNavController()?.navigate(action)



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


}