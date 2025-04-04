package com.sarrawi.chat.adapter

import android.graphics.Color
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sarrawi.chat.MyApplication
import com.sarrawi.chat.R
import com.sarrawi.chat.SharedPrefs
import com.sarrawi.chat.Utils
import com.sarrawi.chat.modal.Messages
import com.sarrawi.chat.modal.RecentChats
import com.sarrawi.chat.modal.Users
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import de.hdodenhof.circleimageview.CircleImageView

class RecentChatAdapter(private val onSelectionChanged: (Boolean) -> Unit) : RecyclerView.Adapter<MyChatListHolder>() {

    private var listOfChats = listOf<RecentChats>()
    fun getList(): List<RecentChats> {
        return listOfChats
    }

    private var listener: onChatClicked? = null
    var chatShitModal = RecentChats()

    private val selectedMessages = mutableSetOf<RecentChats>()
    private var selectionMode = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyChatListHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.recentchatlist, parent, false)
        return MyChatListHolder(view)


    }

    override fun getItemCount(): Int {

        return listOfChats.size


    }


    fun setList(list: List<RecentChats>) {
        this.listOfChats = list


    }

    private fun isSelected(recenet: RecentChats) = selectedMessages.contains(recenet)

    fun getSelectedMessages(): List<RecentChats> = selectedMessages.toList()

    private fun toggleSelection(recenet: RecentChats) {
        if (selectedMessages.contains(recenet)) {
            selectedMessages.remove(recenet)
        } else {
            selectedMessages.add(recenet)
        }
        selectionMode = selectedMessages.isNotEmpty()
        onSelectionChanged(selectionMode)
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedMessages.clear()
        selectionMode = false
        onSelectionChanged(false)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyChatListHolder, position: Int) {

        val chatlist = listOfChats[position]


        chatShitModal = chatlist


        holder.userName.setText(chatlist.name)


        val themessage = chatlist.message!!.split(" ").take(4).joinToString(" ")
        val makelastmessage = "${chatlist.person}: ${themessage} "

        holder.lastMessage.setText(makelastmessage)

        Glide.with(holder.itemView.context).load(chatlist.friendsimage).into(holder.imageView)

        holder.timeView.setText(chatlist.time!!.substring(0, 5))

        holder.itemView.setOnClickListener {
            listener?.getOnChatCLickedItem(position, chatlist)


        }

        holder.itemView.setOnLongClickListener {
            toggleSelection(chatlist)
            true
        }

        holder.itemView.setOnClickListener {
            if (selectionMode) toggleSelection(chatlist)
        }


        holder.itemView.setBackgroundColor(if (isSelected(chatlist)) Color.LTGRAY else Color.TRANSPARENT)



    }


    fun setOnChatClickListener(listener: onChatClicked) {
        this.listener = listener
    }



}

class MyChatListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    val imageView: CircleImageView = itemView.findViewById(R.id.recentChatImageView)
    val userName: TextView = itemView.findViewById(R.id.recentChatTextName)
    val lastMessage: TextView = itemView.findViewById(R.id.recentChatTextLastMessage)
    val timeView: TextView = itemView.findViewById(R.id.recentChatTextTime)


}


interface onChatClicked {
    fun getOnChatCLickedItem(position: Int, chatList: RecentChats)
}
