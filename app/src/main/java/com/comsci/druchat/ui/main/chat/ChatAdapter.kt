package com.comsci.druchat.ui.main.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adedom.library.extension.loadCircle
import com.comsci.druchat.R
import com.comsci.druchat.data.models.User
import com.comsci.druchat.util.KEY_DEFAULT

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.ChatHolder>() {

    private var items = ArrayList<User>()
    var onClick: ((String) -> Unit)? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): ChatHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_chat_list, viewGroup, false)
        return ChatHolder(view)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ChatHolder, position: Int) {
        val user = items[position]
        if (user.imageURL != KEY_DEFAULT) holder.mImgProfile.loadCircle(user.imageURL)
        holder.mTvName.text = user.name

//            holder.mTvCount.text = count.toString()
//            holder.mTvName.setTypeface(null, Typeface.BOLD)
    }

    fun setList(items: List<User>) {
        this.items = items as ArrayList<User>
        notifyDataSetChanged()
    }

    inner class ChatHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val mImgProfile: ImageView = itemView.findViewById(R.id.mImgProfile)
        val mTvName: TextView = itemView.findViewById(R.id.mTvName)
        val mTvCount: TextView = itemView.findViewById(R.id.mTvCount)

        init {
            itemView.setOnClickListener {
                val userId = items[adapterPosition].user_id
                onClick?.invoke(userId)
            }
        }

    }
}

