package com.sarrawi.chat.modal

data class Messages(
    val sender : String? = "",
    val receiver: String? = "",
    val message: String? = "",
    var time: String? = "",


    ) {

    val id : String get() = "$sender-$receiver-$message-$time"
}