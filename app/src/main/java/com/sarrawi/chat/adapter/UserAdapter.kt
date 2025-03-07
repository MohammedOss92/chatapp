package com.sarrawi.chat.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sarrawi.chat.R
import com.sarrawi.chat.modal.Users
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter : RecyclerView.Adapter<UserHolder>() {

    private var listOfUsers = listOf<Users>()
    private var listener: OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.userlistitem, parent, false)
        return UserHolder(view)

    }

    override fun getItemCount(): Int {

        return listOfUsers.size



    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {

        val users = listOfUsers[position]

//        val name = users.username!!.split("\\s".toRegex())[0]
//        holder.profileName.setText(name)
        val name = users.username?.split("\\s".toRegex())?.get(0) ?: "Unknown"
        holder.profileName.text = users.username

        if (users.status.equals("Online")){

            holder.statusImageView.setImageResource(R.drawable.onlinestatus)


        } else {
            holder.statusImageView.setImageResource(R.drawable.offlinestatus)


        }

//        holder.imageProfile.setBackgroundResource(R.drawable.person)
        if (!users.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(users.imageUrl)
                .placeholder(R.drawable.person) // صورة افتراضية في حال لم تكن هناك صورة
                .error(R.drawable.person) // صورة افتراضية في حال حدث خطأ أثناء التحميل
                .into(holder.imageProfile)
        } else {
            holder.imageProfile.setImageResource(R.drawable.person) // صورة افتراضية عند عدم وجود صورة
        }

        holder.itemView.setOnClickListener {
            listener?.onUserSelected(position, users)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: List<Users>){
        this.listOfUsers = list
        notifyDataSetChanged()
    }

    fun setOnClickListener(listener: OnItemClickListener){
        this.listener = listener
    }


}

class UserHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

    val profileName: TextView = itemView.findViewById(R.id.userName)
    val imageProfile : CircleImageView = itemView.findViewById(R.id.imageViewUser)
    val statusImageView: ImageView = itemView.findViewById(R.id.statusOnline)



}

interface OnItemClickListener{
    fun onUserSelected(position: Int, users: Users)
}