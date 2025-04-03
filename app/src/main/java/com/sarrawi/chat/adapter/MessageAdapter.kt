
package com.sarrawi.chat.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sarrawi.chat.R
import com.sarrawi.chat.Utils
import com.sarrawi.chat.Utils.Companion.MESSAGE_LEFT
import com.sarrawi.chat.Utils.Companion.MESSAGE_RIGHT
import com.sarrawi.chat.modal.Messages


import androidx.recyclerview.widget.DiffUtil

class MessageAdapter(private val onSelectionChanged: (Boolean) -> Unit) : RecyclerView.Adapter<MessageHolder>() {

    private var listOfMessage = listOf<Messages>()
    fun getList(): List<Messages> {
        return listOfMessage
    }
    private val LEFT = 0
    private val RIGHT = 1
    private val selectedMessages = mutableSetOf<Messages>()
    private var selectionMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == RIGHT) {
            val view = inflater.inflate(R.layout.chatitemright, parent, false)
            MessageHolder(view)
        } else {
            val view = inflater.inflate(R.layout.chatitemleft, parent, false)
            MessageHolder(view)
        }
    }

    override fun getItemCount() = listOfMessage.size
    private fun isSelected(message: Messages) = selectedMessages.contains(message)

    fun getSelectedMessages(): List<Messages> = selectedMessages.toList()

    private fun toggleSelection(message: Messages) {
        if (selectedMessages.contains(message)) {
            selectedMessages.remove(message)
        } else {
            selectedMessages.add(message)
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

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = listOfMessage[position]

        holder.messageText.visibility = View.VISIBLE
        holder.timeOfSent.visibility = View.VISIBLE

        holder.messageText.text = message.message
        holder.timeOfSent.text = message.time?.substring(0, 5) ?: ""
        holder.date.text = message.date

//        holder.timeOfSent.text = "${message.date} ${message.time?.substring(0, 5) ?: ""}" // عرض التاريخ مع الوقت، فقط الساعات والدقائق

        holder.itemView.setOnLongClickListener {
            toggleSelection(message)
            true
        }

        holder.itemView.setOnClickListener {
            if (selectionMode) toggleSelection(message)
        }


        holder.itemView.setBackgroundColor(if (isSelected(message)) Color.LTGRAY else Color.TRANSPARENT)






    }

    override fun getItemViewType(position: Int) =
        if (listOfMessage[position].sender == Utils.getUidLoggedIn()) RIGHT else LEFT

    fun setList(newList: List<Messages>) {

        this.listOfMessage = newList

    }

}

class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView) {
    val messageText: TextView = itemView.findViewById(R.id.show_message)
    val timeOfSent: TextView = itemView.findViewById(R.id.timeView)
    val date: TextView = itemView.findViewById(R.id.date)
}


/*2
*
* package com.sarrawi.chat.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.sarrawi.chat.R
import com.sarrawi.chat.Utils
import com.sarrawi.chat.modal.Messages

class MessageAdapter(private val onSelectionChanged: (Boolean) -> Unit) : RecyclerView.Adapter<MessageHolder>() {

    private var listOfMessage = listOf<Messages>()
    private val selectedMessages = mutableSetOf<Messages>()
    private var selectionMode = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = if (viewType == MESSAGE_RIGHT) {
            inflater.inflate(R.layout.chatitemright, parent, false)
        } else {
            inflater.inflate(R.layout.chatitemleft, parent, false)
        }
        return MessageHolder(view)
    }

    override fun getItemCount() = listOfMessage.size

    private fun isSelected(message: Messages) = selectedMessages.contains(message)

    fun getSelectedMessages(): List<Messages> = selectedMessages.toList()

    private fun toggleSelection(message: Messages) {
        val position = listOfMessage.indexOf(message)
        if (position != -1) {
            if (selectedMessages.contains(message)) {
                selectedMessages.remove(message)
            } else {
                selectedMessages.add(message)
            }
            selectionMode = selectedMessages.isNotEmpty()
            onSelectionChanged(selectionMode)
            notifyItemChanged(position)  // ✅ تحديث العنصر المحدد فقط بدلاً من إعادة تحميل القائمة بالكامل
        }
    }

    fun clearSelection() {
        selectedMessages.clear()
        selectionMode = false
        onSelectionChanged(false)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val message = listOfMessage[position]

        holder.messageText.text = message.message
        holder.timeOfSent.text = message.time?.substring(0, 5) ?: ""
        holder.date.text = message.date

        // تغيير لون الخلفية عند التحديد
        holder.itemView.setBackgroundColor(if (isSelected(message)) Color.LTGRAY else Color.TRANSPARENT)

        // الضغط المطول لتفعيل وضع التحديد
        holder.itemView.setOnLongClickListener {
            toggleSelection(message)
            true
        }

        // الضغط العادي أثناء التحديد يسمح باختيار المزيد
        holder.itemView.setOnClickListener {
            if (selectionMode) toggleSelection(message)
        }
    }

    override fun getItemViewType(position: Int) =
        if (listOfMessage[position].sender == Utils.getUidLoggedIn()) MESSAGE_RIGHT else MESSAGE_LEFT

    fun setList(newList: List<Messages>) {
        val diffCallback = MessageDiffCallback(listOfMessage, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        listOfMessage = newList
        diffResult.dispatchUpdatesTo(this)
    }
}

// ✅ استخدام ViewHolder مخصص
class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val messageText: TextView = itemView.findViewById(R.id.show_message)
    val timeOfSent: TextView = itemView.findViewById(R.id.timeView)
    val date: TextView = itemView.findViewById(R.id.date)
}

// ✅ إضافة DiffUtil لمقارنة القوائم
class MessageDiffCallback(
    private val oldList: List<Messages>,
    private val newList: List<Messages>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id  // مقارنة الرسائل عبر الـ ID
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]  // مقارنة المحتوى بالكامل
    }
}
*/


//package com.sarrawi.chat.adapter
//
//import android.annotation.SuppressLint
//import android.view.Gravity
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.sarrawi.chat.R
//import com.sarrawi.chat.Utils
//import com.sarrawi.chat.Utils.Companion.MESSAGE_LEFT
//import com.sarrawi.chat.Utils.Companion.MESSAGE_RIGHT
//import com.sarrawi.chat.modal.Messages
//
//
//import androidx.recyclerview.widget.DiffUtil
//
//class MessageAdapter : RecyclerView.Adapter<MessageHolder>() {
//
//    private var listOfMessage = listOf<Messages>()
//
//    private val LEFT = 0
//    private val RIGHT = 1
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        return if (viewType == RIGHT) {
//            val view = inflater.inflate(R.layout.chatitemright, parent, false)
//            MessageHolder(view)
//        } else {
//            val view = inflater.inflate(R.layout.chatitemleft, parent, false)
//            MessageHolder(view)
//        }
//    }
//
//    override fun getItemCount() = listOfMessage.size
//
//    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
//        val message = listOfMessage[position]
//
//        holder.messageText.visibility = View.VISIBLE
//        holder.timeOfSent.visibility = View.VISIBLE
//
//        holder.messageText.text = message.message
//        holder.timeOfSent.text = message.time?.substring(0, 5) ?: ""
//        holder.date.text = message.date
//
////        holder.timeOfSent.text = "${message.date} ${message.time?.substring(0, 5) ?: ""}" // عرض التاريخ مع الوقت، فقط الساعات والدقائق
//
//
//
//
//
//
//
//
//
//
//    }
//
//    override fun getItemViewType(position: Int) =
//        if (listOfMessage[position].sender == Utils.getUidLoggedIn()) RIGHT else LEFT
//
//    fun setList(newList: List<Messages>) {
//
//        this.listOfMessage = newList
//
//    }
//
//}
//
//class MessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView.rootView) {
//    val messageText: TextView = itemView.findViewById(R.id.show_message)
//    val timeOfSent: TextView = itemView.findViewById(R.id.timeView)
//    val date: TextView = itemView.findViewById(R.id.date)
//}
//
