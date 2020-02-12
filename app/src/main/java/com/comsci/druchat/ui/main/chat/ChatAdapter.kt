package com.comsci.druchat.ui.main.chat

import com.adedom.library.extension.loadCircle
import com.adedom.library.util.BaseRecyclerViewAdapter
import com.comsci.druchat.R
import com.comsci.druchat.data.models.User
import com.comsci.druchat.util.KEY_DEFAULT
import kotlinx.android.synthetic.main.item_chat_list.view.*

class ChatAdapter : BaseRecyclerViewAdapter<User>({ R.layout.item_chat_list },
    { holder, position, items ->
        val user = items[position]
        if (user.imageURL != KEY_DEFAULT) holder.itemView.mImgProfile.loadCircle(user.imageURL)
        holder.itemView.mTvName.text = user.name

//            holder.mTvCount.text = count.toString()
//            holder.mTvName.setTypeface(null, Typeface.BOLD)
    })

