package com.comsci.druchat.fragments

import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.adedom.library.extension.*
import com.comsci.druchat.MainActivity
import com.comsci.druchat.MessageActivity
import com.comsci.druchat.R
import com.comsci.druchat.data.models.Follow
import com.comsci.druchat.util.*

class HomeFragment : BaseFragment({R.layout.fragment_home}) {

    private lateinit var mAdapter: FollowAdapter

    private lateinit var mIvProfile: ImageView
    private lateinit var mTvName: TextView
    private lateinit var mTvStatus: TextView

    override fun initFragment(view: View) {
        super.initFragment(view)
        mIvProfile = view.findViewById(R.id.mImgProfile) as ImageView
        mTvName = view.findViewById(R.id.mTvName) as TextView
        mTvStatus = view.findViewById(R.id.mTvStatus) as TextView
        val etSearch = view.findViewById(R.id.mEtSearch) as EditText
        val recyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView

        mAdapter = FollowAdapter()

        recyclerView.recyclerVertical { it.adapter = mAdapter }

        mAdapter.follow = {
            AlertDialog.Builder(activity!!).dialogPositive(
                R.string.follow_friend,
                "Do you want to follow ${it.name} ?",
                R.drawable.ic_person_add_black
            ) { updateFollow(it.user_id, KEY_FOLLOW) }
        }

        mAdapter.unfollow = {
            //            "Do you want to cancel follow ${mUserItem[adapterPosition].name} ?"
            AlertDialog.Builder(activity!!).dialogNegative(R.string.un_follow_friend) {
                updateFollow(it.user_id, KEY_UN_FOLLOW)
            }
        }

        mAdapter.chat = {
            startActivity(
                Intent(MainActivity.sContext, MessageActivity::class.java)
                    .putExtra(KEY_USER_ID, it.user_id)
            )
        }

        etSearch.textChanged {
            if (it.isEmpty()) fetchFollow() else fetchSearch(it)
        }

        fetchProfile()
        fetchFollow()
    }

    private fun fetchProfile() {
        viewModel.getUser().observe(this, Observer {
            if (it.imageURL != KEY_DEFAULT) mIvProfile.loadCircle(it.imageURL)
            mTvName.text = it.name
            mTvStatus.text = it.status
        })
    }

    private fun fetchFollow() {
        mAdapter.typeList = KEY_FOLLOW
        viewModel.getFollows().observe(this, Observer {
            mAdapter.setList(it)
        })
    }

    private fun fetchSearch(name: String) {
        mAdapter.typeList = KEY_SEARCH
        viewModel.getSearch(name).observe(this, Observer {
            mAdapter.setList(it)
        })
    }

    private fun updateFollow(userId: String, type: String) {
        val friend = Follow(userId, type)
        viewModel.setFollow(userId, friend)
    }
}
