package com.sarrawi.chat.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.sarrawi.chat.R
import com.sarrawi.chat.Utils
import com.sarrawi.chat.adapter.MessageAdapter
import com.sarrawi.chat.databinding.FragmentChatfromHomeBinding
import com.sarrawi.chat.modal.Messages
import com.sarrawi.chat.mvvm.ChatAppViewModel
import de.hdodenhof.circleimageview.CircleImageView

class ChatfromHome : Fragment() {

    private lateinit var args: ChatfromHomeArgs
    private lateinit var binding: FragmentChatfromHomeBinding
    private lateinit var viewModel: ChatAppViewModel
    private lateinit var adapter: MessageAdapter
    private var actionMode: ActionMode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chatfrom_home, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<Toolbar>(R.id.toolBarChat)
        val circleImageView = toolbar.findViewById<CircleImageView>(R.id.chatImageViewUser)
        val textViewName = toolbar.findViewById<TextView>(R.id.chatUserName)

        args = ChatfromHomeArgs.fromBundle(requireArguments())
        viewModel = ViewModelProvider(this)[ChatAppViewModel::class.java]

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        Glide.with(view.context)
            .load(args.recentchats.friendsimage)
            .placeholder(R.drawable.person)
            .dontAnimate()
            .into(circleImageView)

        textViewName.text = args.recentchats.name

        binding.chatBackBtn.setOnClickListener {
            view.findNavController().navigate(R.id.action_chatfromHome_to_homeFragment)
        }

        binding.sendBtn.setOnClickListener {
            viewModel.sendMessage(
                Utils.getUidLoggedIn(),
                args.recentchats.friendid!!,
                args.recentchats.name!!,
                args.recentchats.friendsimage!!
            )
        }

        viewModel.getMessages(args.recentchats.friendid!!).observe(viewLifecycleOwner, Observer {
            initRecyclerView(it)
        })
    }

    private fun initRecyclerView(list: List<Messages>) {
        adapter = MessageAdapter { isSelectionActive -> toggleActionMode(isSelectionActive) }

        val layoutManager = LinearLayoutManager(context)
        binding.messagesRecyclerView.layoutManager = layoutManager
        layoutManager.stackFromEnd = true

        adapter.setList(list)
        binding.messagesRecyclerView.adapter = adapter
    }

    private fun toggleActionMode(isSelectionActive: Boolean) {
        if (isSelectionActive) {
            if (actionMode == null) {
                // استدعاء startSupportActionMode من AppCompatActivity
                actionMode = (requireActivity() as AppCompatActivity).startSupportActionMode(actionModeCallback)
            }
            actionMode?.title = "${adapter.getSelectedMessages().size} محدد"
        } else {
            actionMode?.finish()
        }
    }




    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            requireActivity().menuInflater.inflate(R.menu.selection_menu, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = false

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when (item?.itemId) {
                R.id.menu_copy -> copyMessages()
                R.id.menu_share -> shareMessages()
                R.id.menu_delete -> deleteMessages()
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            adapter.clearSelection()
            actionMode = null
        }
    }

    private fun copyMessages() {
        // جمع النصوص المرسلة في الرسائل المحددة
        val messagesText = adapter.getSelectedMessages().joinToString("\n") { it.message.orEmpty() }

        // فحص إذا كان الجهاز يدعم الـ API 23 أو أعلى
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // إذا كان الجهاز يدعم API 23 أو أعلى
            val clipboard = requireActivity().getSystemService(ClipboardManager::class.java)
            val clip = ClipData.newPlainText("Chat Messages", messagesText)
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "تم النسخ", Toast.LENGTH_SHORT).show()
        } else {
            // في حالة الأجهزة التي تعمل بنظام أقل من API 23
            val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Chat Messages", messagesText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "تم النسخ", Toast.LENGTH_SHORT).show()
        }

        // إنهاء وضع الاختيار (ActionMode)
        actionMode?.finish()
    }

    private fun shareMessages() {
        val messagesText = adapter.getSelectedMessages().joinToString("\n") { it.message.orEmpty() }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, messagesText)
        }
        startActivity(Intent.createChooser(intent, "مشاركة عبر"))
        actionMode?.finish()
    }

    private fun deleteMessages() {
        val selectedMessages = adapter.getSelectedMessages()
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        for (message in selectedMessages) {
            val docRef = db.collection("messages").document(message.id)
            batch.delete(docRef)
        }

        batch.commit().addOnSuccessListener {
            Toast.makeText(requireContext(), "تم الحذف", Toast.LENGTH_SHORT).show()
            adapter.setList(adapter.getList().filterNot { it in selectedMessages })
            adapter.notifyDataSetChanged()
            actionMode?.finish()
        }
    }
}


//package com.sarrawi.chat.fragments
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.appcompat.widget.Toolbar
//import androidx.databinding.DataBindingUtil
//import androidx.lifecycle.Observer
//import androidx.lifecycle.ViewModelProvider
//import androidx.navigation.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.bumptech.glide.Glide
//import com.sarrawi.chat.R
//import com.sarrawi.chat.Utils
//import com.sarrawi.chat.adapter.MessageAdapter
//import com.sarrawi.chat.databinding.FragmentChatfromHomeBinding
//import com.sarrawi.chat.modal.Messages
//import com.sarrawi.chat.mvvm.ChatAppViewModel
//import de.hdodenhof.circleimageview.CircleImageView
//
//class ChatfromHome : Fragment() {
//
//
//
//    lateinit var args : ChatfromHomeArgs
//    lateinit var binding: FragmentChatfromHomeBinding
//    lateinit var viewModel : ChatAppViewModel
//    lateinit var toolbar: Toolbar
//    lateinit var adapter : MessageAdapter
//
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chatfrom_home, container, false)
//
//
//        return binding.root
//
//    }
//
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//        toolbar = view.findViewById(R.id.toolBarChat)
//        val circleImageView = toolbar.findViewById<CircleImageView>(R.id.chatImageViewUser)
//        val textViewName = toolbar.findViewById<TextView>(R.id.chatUserName)
//
//
//        args = ChatfromHomeArgs.fromBundle(requireArguments())
//
//
//        viewModel = ViewModelProvider(this).get(ChatAppViewModel::class.java)
//
//
//        binding.viewModel = viewModel
//        binding.lifecycleOwner = viewLifecycleOwner
//
//
//        Glide.with(view.getContext()).load(args.recentchats.friendsimage!!).placeholder(R.drawable.person).dontAnimate().into(circleImageView);
//        textViewName.setText(args.recentchats.name)
//        //textViewStatus.setText(args.users.status)
//
//
//
//        binding.chatBackBtn.setOnClickListener {
//
//
//            view.findNavController().navigate(R.id.action_chatfromHome_to_homeFragment)
//
//        }
//
//        binding.sendBtn.setOnClickListener {
//
//
//            viewModel.sendMessage(Utils.getUidLoggedIn(), args.recentchats.friendid!!, args.recentchats.name!!, args.recentchats.friendsimage!!)
//
//
//
//
//
//        }
//
//
//        viewModel.getMessages(args.recentchats.friendid!!).observe(viewLifecycleOwner, Observer {
//
//
//
//
//
//            initRecyclerView(it)
//
//
//
//        })
//
//
//
//
//    }
//
//    private fun initRecyclerView(list: List<Messages>) {
//
//
//        adapter = MessageAdapter()
//
//        val layoutManager = LinearLayoutManager(context)
//
//        binding.messagesRecyclerView.layoutManager = layoutManager
//        layoutManager.stackFromEnd = true
//
//        adapter.setList(list)
//        adapter.notifyDataSetChanged()
//        binding.messagesRecyclerView.adapter = adapter
//
//
//
//    }
//
//
//}