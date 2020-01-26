package com.comsci.druchat.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adedom.library.extension.loadCircle
import com.comsci.druchat.MainActivity
import com.comsci.druchat.MessageActivity
import com.comsci.druchat.R
import com.comsci.druchat.model.ChatListItem
import com.comsci.druchat.model.ChatsItem
import com.comsci.druchat.model.UserItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.util.*

class ChatsFragment : Fragment() {

    private lateinit var mUser: FirebaseUser
    private lateinit var mDatabaseUser: DatabaseReference
    private lateinit var mDatabaseChat: DatabaseReference
    private lateinit var mDatabaseChatList: DatabaseReference
    private lateinit var mUserItem: ArrayList<UserItem>
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mUnread: HashMap<String, String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        mUser = FirebaseAuth.getInstance().currentUser!!
        mDatabaseUser = FirebaseDatabase.getInstance().getReference("Users")
        mDatabaseChat = FirebaseDatabase.getInstance().getReference("Chats")
        mDatabaseChatList = FirebaseDatabase.getInstance().getReference("ChatList")

        bindWidgets(view)

        mUserItem = arrayListOf<UserItem>()
        mUnread = HashMap<String, String>()
        mRecyclerView.layoutManager =
            LinearLayoutManager(MainActivity.sContext)

        return view
    }

    private fun bindWidgets(view: View) {
        mRecyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView
    }

    private fun feedChatList() {
        mDatabaseChatList.child(mUser.uid).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                mUserItem.clear()
                mUnread.clear()

                val chatListItem = arrayListOf<ChatListItem>()
                for (snapshot in dataSnapshot.children) {
                    val item = snapshot.getValue(ChatListItem::class.java)!!
                    chatListItem.add(item)
                }
                chatListItem.sortWith(compareBy(ChatListItem::key))
                chatListItem.reverse()

                for (item in chatListItem) {
                    mDatabaseUser.child(item.user_id)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val user = dataSnapshot.getValue(UserItem::class.java)!!

                                mUserItem.add(user)
                                mRecyclerView.adapter = CustomAdapter()
                            }
                        })

                    mDatabaseChat.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var unread = 0
                            for (snapshot in dataSnapshot.children) {
                                val chat = snapshot.getValue(ChatsItem::class.java)!!
                                if (chat.sender == item.user_id && !chat.isread) {
                                    unread++
                                }
                            }

                            mUnread["id"] = item.user_id
                            mUnread["count"] = unread.toString()

                            mRecyclerView.adapter = CustomAdapter()
                        }
                    })
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        feedChatList()
    }

    inner class CustomAdapter : RecyclerView.Adapter<CustomHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): CustomHolder {
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.item_chat_list, viewGroup, false)
            return CustomHolder(view)
        }

        override fun getItemCount(): Int {
            return mUserItem.size
        }

        override fun onBindViewHolder(holder: CustomHolder, position: Int) {
            val user = mUserItem[position]
            if (user.imageURL != "default") holder.mImgProfile.loadCircle(user.imageURL)
            holder.mTvName.text = user.name

            if (mUnread["id"] == user.user_id) {
                val count: Int = mUnread["count"]!!.toInt()

                if (count == 0) {
                    holder.mTvCount.visibility = View.GONE
                } else {
                    holder.mTvCount.text = count.toString()
                    holder.mTvName.setTypeface(null, Typeface.BOLD)
                }
            }
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
                        .putExtra("user_id", userId)
                )
            }
        }
    }
}
