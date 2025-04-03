package com.sarrawi.chat.fragments

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.sarrawi.chat.R
import com.sarrawi.chat.Utils
import com.sarrawi.chat.adapter.MessageAdapter
import com.sarrawi.chat.databinding.FragmentChatBinding
import com.sarrawi.chat.modal.Messages
import com.sarrawi.chat.mvvm.ChatAppViewModel
import de.hdodenhof.circleimageview.CircleImageView


class ChatFragment : Fragment() {


    lateinit var args: ChatFragmentArgs
    lateinit var binding : FragmentChatBinding

    lateinit var viewModel : ChatAppViewModel
    lateinit var adapter : MessageAdapter
    lateinit var toolbar: Toolbar
    private var actionMode: ActionMode? = null





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chat, container, false)

        return binding.root
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.toolBarChat)
        val circleImageView = toolbar.findViewById<CircleImageView>(R.id.chatImageViewUser)
        val textViewName = toolbar.findViewById<TextView>(R.id.chatUserName)
        val textViewStatus = view.findViewById<TextView>(R.id.chatUserStatus)
        val chatBackBtn = toolbar.findViewById<ImageView>(R.id.chatBackBtn)

        viewModel = ViewModelProvider(this).get(ChatAppViewModel::class.java)


        args = ChatFragmentArgs.fromBundle(requireArguments())

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner



        Glide.with(view.getContext()).load(args.users.imageUrl!!).placeholder(R.drawable.person).dontAnimate().into(circleImageView);
        textViewName.setText(args.users.username)
        textViewStatus.setText(args.users.status)


        chatBackBtn.setOnClickListener {


            view.findNavController().navigate(R.id.action_chatFragment_to_homeFragment)

        }

        binding.sendBtn.setOnClickListener {


            viewModel.sendMessage(Utils.getUidLoggedIn(), args.users.userid!!, args.users.username!!, args.users.imageUrl!!)





        }



        viewModel.getMessages(args.users.userid!!).observe(viewLifecycleOwner, Observer {





            initRecyclerView(it)



        })




    }

    private fun initRecyclerView(list: List<Messages>) {


//        adapter = MessageAdapter()
        adapter = MessageAdapter { isSelectionActive -> toggleActionMode(isSelectionActive) }

        val layoutManager = LinearLayoutManager(context)

        binding.messagesRecyclerView.layoutManager = layoutManager
        layoutManager.stackFromEnd = true

        adapter.setList(list)
        adapter.notifyDataSetChanged()
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