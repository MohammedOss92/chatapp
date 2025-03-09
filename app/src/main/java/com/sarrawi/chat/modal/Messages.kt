package com.sarrawi.chat.modal

data class Messages(
    val sender : String? = "",
    val receiver: String? = "",
    val message: String? = "",
    val time: String? = "",
//    val date: String? = ""   // تاريخ الرسالة

    ) {

    val id : String get() = "$sender-$receiver-$message-$time"
//    fun setDate() {
//        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())  // تنسيق التاريخ
//        val date: String = formatter.format(Date(System.currentTimeMillis()))
//        this.date = date  // تعيين التاريخ
//    }
}