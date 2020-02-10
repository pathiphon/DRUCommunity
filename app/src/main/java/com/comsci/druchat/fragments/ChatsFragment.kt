package com.comsci.druchat.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adedom.library.extension.loadCircle
import com.comsci.druchat.MainActivity
import com.comsci.druchat.MessageActivity
import com.comsci.druchat.R
import com.comsci.druchat.data.models.Users
import com.comsci.druchat.data.viewmodel.BaseViewModel

class ChatsFragment : Fragment() {

    val TAG = "MyTag"

    private lateinit var viewModel: BaseViewModel

    private lateinit var mUserItem: ArrayList<Users>
    private lateinit var mRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        init(view)

        mUserItem = arrayListOf<Users>()

        return view
    }

    private fun init(view: View) {
        mRecyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView
        mRecyclerView.layoutManager = LinearLayoutManager(MainActivity.sContext)

        //todo count no read
        viewModel.getChatListUsers().observe(this, Observer {
            Log.d(TAG, ">>${it}")
            mUserItem = it as ArrayList<Users>
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
            if (user.imageURL != "default") holder.mImgProfile.loadCircle(user.imageURL)
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
                        .putExtra("user_id", userId)
                )
            }
        }
    }
}
