package com.sarrawi.chat.modal

import java.text.SimpleDateFormat
import java.util.*

data class Messages(
    val sender : String? = "",
    val receiver: String? = "",
    val message: String? = "",
    val time: String? = "",
    var date: String? = ""   // تاريخ الرسالة

    ) {

    val id : String get() = "$sender-$receiver-$message-$time"
    fun setDate() {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())  // تنسيق التاريخ
        val date: String = formatter.format(Date(System.currentTimeMillis()))
        this.date = date  // تعيين التاريخ
    }
}