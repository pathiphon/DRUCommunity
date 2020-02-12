package com.comsci.druchat.ui.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adedom.library.extension.getLocality
import com.adedom.library.extension.loadBitmap
import com.adedom.library.extension.loadImage
import com.comsci.druchat.R
import com.comsci.druchat.data.models.Messages
import com.comsci.druchat.util.MessagesType

class MessagesAdapter( private val userId: String) :
    RecyclerView.Adapter<MessagesAdapter.MessagesHolder>() {

    private var items = ArrayList<Messages>()
    private lateinit var MSG_TYPE: MessagesType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view: View = if (MSG_TYPE == MessagesType.CHAT_RIGHT) {
            layoutInflater.inflate(R.layout.item_chat_right, parent, false)
        } else {
            layoutInflater.inflate(R.layout.item_chat_left, parent, false)
        }
        return MessagesHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MessagesHolder, position: Int) {
        val chat = items[position]
        // message
        holder.mTvMessage.text = chat.message

        //time
        holder.mTvTime.text = datetimeFormat("time", chat.dateTime)

        //date
        if (position > 0 && datetimeFormat("date", chat.dateTime) ==
            datetimeFormat("date", items[position - 1].dateTime)
        ) {
            holder.mTvDate.visibility = View.GONE
        } else {
            holder.mTvDate.text = datetimeFormat("date", chat.dateTime)
        }

        //read
        if (chat.isread) {
            holder.mTvRead.visibility = View.VISIBLE
        }

        //image
        if (chat.message.isEmpty()) {
            holder.mTvMessage.visibility = View.GONE
            holder.mImgImage.visibility = View.VISIBLE
            holder.mImgImage.loadBitmap(chat.image, {
                if (it.width > it.height) holder.mImgImage.layoutParams.height = 300
                holder.mImgImage.loadImage(chat.image)
            })
        }

        //location
        val context = holder.itemView.context
        if (position > 0 && context.getLocality(chat.latitude, chat.longitude) ==
            context.getLocality(items[position - 1].latitude, items[position - 1].longitude)
        ) {
            holder.mTvLocation.visibility = View.GONE
        } else {
            holder.mTvLocation.text = context.getLocality(chat.latitude, chat.longitude)
        }
    }

    private fun datetimeFormat(format: String, dateTime: String): String {
//        "EEE, dd MMM yy HH:mm"
        return if (format == "date") {
            dateTime.substring(0, 15).trim()
        } else {
            dateTime.substring(15, dateTime.length).trim()
        }
    }

    override fun getItemViewType(position: Int): Int {
        MSG_TYPE = if (items[position].sender == userId) {
            MessagesType.CHAT_RIGHT
        } else {
            MessagesType.CHAT_LEFT
        }
        return super.getItemViewType(position)
    }

    fun setList(items: List<Messages>) {
        this.items = items as ArrayList<Messages>
        notifyDataSetChanged()
    }

    inner class MessagesHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImgImage: ImageView = itemView.findViewById(R.id.mImgImage)
        val mTvMessage: TextView = itemView.findViewById(R.id.mTvMessage)
        val mTvRead: TextView = itemView.findViewById(R.id.mTvRead)
        val mTvTime: TextView = itemView.findViewById(R.id.mTvTime)
        val mTvDate: TextView = itemView.findViewById(R.id.mTvDate)
        val mTvLocation: TextView = itemView.findViewById(R.id.mTvLocation)
    }

}
