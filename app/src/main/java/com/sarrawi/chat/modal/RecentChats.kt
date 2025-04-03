package com.sarrawi.chat.modal

import android.os.Parcel
import android.os.Parcelable
data class RecentChats(
    val id: String = "",  // إضافة معرف فريد للمحادثة
    val friendid: String? = "",
    val friendsimage: String? = "",
    val time: String? = "",
    val name: String? = "",
    val sender: String? = "",
    val receiver: String = "",
    val message: String? = "",
    val person: String? = "",
    val status: String? = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",  // قراءة id
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString() ?: "", // Ensure receiver is not null
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)  // كتابة id
        parcel.writeString(friendid)
        parcel.writeString(friendsimage)
        parcel.writeString(time)
        parcel.writeString(name)
        parcel.writeString(sender)
        parcel.writeString(receiver)
        parcel.writeString(message)
        parcel.writeString(person)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RecentChats> {
        override fun createFromParcel(parcel: Parcel): RecentChats {
            return RecentChats(parcel)
        }

        override fun newArray(size: Int): Array<RecentChats?> {
            return arrayOfNulls(size)
        }
    }
}

//
//data class RecentChats(
//    val friendid: String? = "",
//    val friendsimage: String? = "",
//    val time: String? = "",
//    val name: String? = "",
//    val sender: String? = "",
//    val receiver: String = "",
//    val message: String? = "",
//    val person: String? = "",
//    val status: String? = ""
//) : Parcelable {
//
//    constructor(parcel: Parcel) : this(
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString() ?: "", // Ensure receiver is not null
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readString()
//    )
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(friendid)
//        parcel.writeString(friendsimage)
//        parcel.writeString(time)
//        parcel.writeString(name)
//        parcel.writeString(sender)
//        parcel.writeString(receiver) // Added receiver
//        parcel.writeString(message)
//        parcel.writeString(person)
//        parcel.writeString(status)
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<RecentChats> {
//        override fun createFromParcel(parcel: Parcel): RecentChats {
//            return RecentChats(parcel)
//        }
//
//        override fun newArray(size: Int): Array<RecentChats?> {
//            return arrayOfNulls(size)
//        }
//    }
//}
