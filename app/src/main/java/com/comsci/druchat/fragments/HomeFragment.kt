package com.comsci.druchat.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.comsci.druchat.MainActivity
import com.comsci.druchat.MessageActivity
import com.comsci.druchat.R
import com.comsci.druchat.model.FollowItem
import com.comsci.druchat.model.UserItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    val TAG = "HomeFragment"
    private var TYPE_FEED = "follow"
    private lateinit var mUser: FirebaseUser
    private lateinit var mDatabaseUsers: DatabaseReference
    private lateinit var mDatabaseFollow: DatabaseReference
    private lateinit var mImgProfile: ImageView
    private lateinit var mTvName: TextView
    private lateinit var mTvStatus: TextView
    private lateinit var mEdtSearch: EditText
    private lateinit var mUserItem: ArrayList<UserItem>
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        mUser = FirebaseAuth.getInstance().currentUser!!
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference("Users")
        mDatabaseFollow = FirebaseDatabase.getInstance().getReference("Follow")

        bindWidgets(view)
        setEvents()

        mUserItem = arrayListOf<UserItem>()
        mRecyclerView.layoutManager =
            LinearLayoutManager(MainActivity.mContext)

        feedDatabase()

        return view
    }

    private fun bindWidgets(view: View) {
        mImgProfile = view.findViewById(R.id.mImgProfile) as ImageView
        mTvName = view.findViewById(R.id.mTvName) as TextView
        mTvStatus = view.findViewById(R.id.mTvStatus) as TextView
        mEdtSearch = view.findViewById(R.id.mEdtSearch) as EditText
        mRecyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView
    }

    private fun feedDatabase() {
        feedProfile()
        feedFollow()
        mDatabaseUsers.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(dataSnapshot: DataSnapshot, p1: String?) {
                val user = dataSnapshot.getValue(UserItem::class.java)!!

                if (TYPE_FEED == "follow") {
                    for (item in mUserItem) {
                        if (user.user_id == item.user_id) {
                            mUserItem.remove(item)
                            mUserItem.add(user)
                            mUserItem.sortWith(compareBy(UserItem::name))
                            mRecyclerView.adapter = CustomAdapter()
                            return
                        }
                    }
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }

    private fun setEvents() {
        mEdtSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(name: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (name!!.isEmpty()) {
                    feedFollow()
                } else {
                    feedSearch(name.toString().trim())
                }
            }
        })
    }

    private fun feedProfile() {
        mDatabaseUsers.child(mUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(UserItem::class.java)!!

                mTvName.text = user.name
                mTvStatus.text = user.status
                if (user.imageURL != "default") {
                    Glide.with(MainActivity.mContext).load(user.imageURL).circleCrop().into(mImgProfile)
                }
            }
        })
    }

    private fun feedFollow() {
        mUserItem.clear()
        TYPE_FEED = "follow"
        mDatabaseFollow.child(mUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val follow = snapshot.getValue(FollowItem::class.java)
                    if (follow!!.type == "follow") {
                        mDatabaseUsers.child(follow.user_id)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    val user = dataSnapshot.getValue(UserItem::class.java)!!

                                    mUserItem.add(user)
                                    mUserItem.sortWith(compareBy(UserItem::name))
                                    mRecyclerView.adapter = CustomAdapter()
                                }
                            })
                    }
                }
            }
        })
    }

    private fun feedSearch(name: String) {
        mUserItem.clear()
        TYPE_FEED = "search"
        val query = mDatabaseUsers.orderByChild("name").startAt(name).endAt(name + "\uf8ff")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val user = snapshot.getValue(UserItem::class.java)
                    if (mUser.uid != user!!.user_id) {
                        mUserItem.add(user)
                    }
                }
                mUserItem.sortWith(compareBy(UserItem::name))
                mRecyclerView.adapter = CustomAdapter()
            }
        })
    }

    inner class CustomAdapter : RecyclerView.Adapter<CustomHolder>() {
        override fun onCreateViewHolder(viewGroup: ViewGroup, p1: Int): CustomHolder {
            val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.item_user, viewGroup, false)
            return CustomHolder(view)
        }

        override fun getItemCount(): Int {
            return mUserItem.size
        }

        override fun onBindViewHolder(holder: CustomHolder, position: Int) {
            val user = mUserItem[position]
            if (user.imageURL != "default") {
                Glide.with(MainActivity.mContext).load(user.imageURL).circleCrop().into(holder.mImgProfile)
            }
            holder.mTvName.text = user.name
            holder.mTvStatus.text = user.status

            if (user.state != "offline") {
                holder.mImgState.setImageResource(R.drawable.shape_state_green)
            }
        }
    }

    inner class CustomHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImgProfile: ImageView = itemView.findViewById(R.id.mImgProfile)
        val mTvName: TextView = itemView.findViewById(R.id.mTvName)
        val mTvStatus: TextView = itemView.findViewById(R.id.mTvStatus)
        val mImgState: ImageView = itemView.findViewById(R.id.mImgState)

        init {
            if (TYPE_FEED == "follow") {
                itemView.setOnLongClickListener {
                    unFollowFriend("un_follow")
                    false
                }

                itemView.setOnClickListener {
                    val follow_id = mUserItem[adapterPosition].user_id
                    startActivity(
                        Intent(MainActivity.mContext, MessageActivity::class.java)
                            .putExtra("user_id", follow_id)
                    )
                }
            } else {
                itemView.setOnClickListener {
                    followFriend("follow")
                }
            }
        }

        private fun followFriend(type: String) {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle(R.string.follow_friend)
                .setMessage("Do you want to follow ${mUserItem[adapterPosition].name} ?")
                .setNegativeButton(R.string.no) { dialogInterface, i ->
                    dialogInterface.dismiss()
                }.setPositiveButton(R.string.yes) { dialogInterface, i ->
                    updateFollow(type)
                }.show()
        }

        private fun unFollowFriend(type: String) {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle(R.string.un_follow_friend)
                .setMessage("Do you want to cancel follow ${mUserItem[adapterPosition].name} ?")
                .setNegativeButton(R.string.yes) { dialogInterface, i ->
                    updateFollow(type)
                }.setPositiveButton(R.string.no) { dialogInterface, i ->
                    dialogInterface.dismiss()
                }.show()
        }

        private fun updateFollow(type: String) {
            val friend = FollowItem(mUserItem[adapterPosition].user_id, type)
            mDatabaseFollow.child(mUser.uid).child(mUserItem[adapterPosition].user_id)
                .setValue(friend).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        feedFollow()
                    }
                }
        }
    }
}
