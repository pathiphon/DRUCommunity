package com.comsci.druchat.fragments

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adedom.library.extension.loadCircle
import com.comsci.druchat.MainActivity
import com.comsci.druchat.MessageActivity
import com.comsci.druchat.R
import com.comsci.druchat.data.models.User
import com.comsci.druchat.util.BaseFragment
import com.comsci.druchat.util.KEY_DEFAULT
import com.comsci.druchat.util.KEY_USER_ID

class ChatsFragment : BaseFragment({ R.layout.fragment_chats }) {

    private lateinit var mUserItem: ArrayList<User>
    private lateinit var mRecyclerView: RecyclerView

    override fun initFragment(view: View) {
        super.initFragment(view)
        mUserItem = ArrayList<User>()

        mRecyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView
        mRecyclerView.layoutManager = LinearLayoutManager(MainActivity.sContext)

        //todo count no read
        viewModel.getChatListUsers().observe(this, Observer {
            mUserItem = it as ArrayList<User>
            mRecyclerView.adapter = CustomAdapter()
        })
    }

    inner class CustomAdapter : RecyclerView.Adapter<CustomHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): CustomHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_chat_list, viewGroup, false)
            return CustomHolder(view)
        }

        override fun getItemCount() = mUserItem.size

        override fun onBindViewHolder(holder: CustomHolder, position: Int) {
            val user = mUserItem[position]
            if (user.imageURL != KEY_DEFAULT) holder.mImgProfile.loadCircle(user.imageURL)
            holder.mTvName.text = user.name

//            holder.mTvCount.text = count.toString()
//            holder.mTvName.setTypeface(null, Typeface.BOLD)
        }
    }

    inner class CustomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImgProfile: ImageView = itemView.findViewById(R.id.mImgProfile)
        val mTvName: TextView = itemView.findViewById(R.id.mTvName)
        val mTvCount: TextView = itemView.findViewById(R.id.mTvCount)

        init {
            itemView.setOnClickListener {
                val userId = mUserItem[adapterPosition].user_id
                startActivity(
                    Intent(MainActivity.sContext, MessageActivity::class.java)
                        .putExtra(KEY_USER_ID, userId)
                )
            }
        }
    }
}
