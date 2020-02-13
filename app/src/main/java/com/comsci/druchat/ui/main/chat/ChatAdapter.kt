package com.comsci.druchat.ui.main.chat

import android.graphics.Typeface
import android.view.View
import com.adedom.library.extension.loadCircle
import com.adedom.library.util.BaseRecyclerViewAdapter
import com.comsci.druchat.R
import com.comsci.druchat.data.models.ChatUser
import com.comsci.druchat.util.KEY_DEFAULT
import kotlinx.android.synthetic.main.item_chat_list.view.*

class ChatAdapter : BaseRecyclerViewAdapter<ChatUser>({ R.layout.item_chat_list },
    { holder, position, items ->
        val user = items[position]
        if (user.imageURL != KEY_DEFAULT) holder.itemView.mImgProfile.loadCircle(user.imageURL)
        holder.itemView.mTvName.text = user.name

        if (user.count == 0) {
            holder.itemView.mTvCount.visibility = View.GONE
        } else {
            holder.itemView.mTvCount.visibility = View.VISIBLE
            holder.itemView.mTvCount.text = user.count.toString()
            holder.itemView.mTvName.setTypeface(null, Typeface.BOLD)
        }
    })

