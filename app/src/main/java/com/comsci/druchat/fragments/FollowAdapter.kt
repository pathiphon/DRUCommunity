package com.comsci.druchat.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adedom.library.extension.loadCircle
import com.comsci.druchat.R
import com.comsci.druchat.data.models.User
import kotlinx.android.synthetic.main.item_user.view.*

class FollowAdapter : RecyclerView.Adapter<FollowAdapter.FollowHolder>() {

    private var items = arrayListOf<User>()
    var follow: ((User) -> Unit)? = null
    var unfollow: ((User) -> Unit)? = null
    var chat: ((User) -> Unit)? = null
    var typeList = "follow"

    override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): FollowHolder {
        return FollowHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.item_user, viewGroup, false)
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: FollowHolder, position: Int) {
        val user = items[position]
        if (user.imageURL != "default") holder.itemView.mImgProfile.loadCircle(user.imageURL)
        holder.itemView.mTvName.text = user.name
        holder.itemView.mTvStatus.text = user.status

        if (user.state != "offline") {
            holder.itemView.mImgState.setImageResource(R.drawable.shape_state_green)
        }
    }

    fun setList(items: List<User>) {
        this.items = items as ArrayList<User>
        this.items.sortWith(compareBy(User::name))
        notifyDataSetChanged()
    }

    inner class FollowHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            if (typeList == "follow") {
                itemView.setOnLongClickListener {
                    unfollow?.invoke(items[adapterPosition])
                    true
                }

                itemView.setOnClickListener { chat?.invoke(items[adapterPosition]) }
            } else itemView.setOnClickListener { follow?.invoke(items[adapterPosition]) }
        }
    }
}

