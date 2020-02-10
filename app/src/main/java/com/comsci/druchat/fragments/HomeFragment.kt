package com.comsci.druchat.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.adedom.library.extension.*
import com.comsci.druchat.MainActivity
import com.comsci.druchat.MessageActivity
import com.comsci.druchat.R
import com.comsci.druchat.data.models.Follows
import com.comsci.druchat.data.viewmodel.BaseViewModel

class HomeFragment : Fragment() {

    private lateinit var viewModel: BaseViewModel
    private lateinit var mAdapter: FollowAdapter

    private lateinit var mImgProfile: ImageView
    private lateinit var mTvName: TextView
    private lateinit var mTvStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        init(view)

        fetchProfile()
        fetchFollow()

        return view
    }

    private fun init(view: View) {
        mImgProfile = view.findViewById(R.id.mImgProfile) as ImageView
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
            ) { updateFollow(it.user_id, "follow") }
        }

        mAdapter.unfollow = {
            //            "Do you want to cancel follow ${mUserItem[adapterPosition].name} ?"
            AlertDialog.Builder(activity!!).dialogNegative(R.string.un_follow_friend) {
                updateFollow(it.user_id, "un_follow")
            }
        }

        mAdapter.chat = {
            startActivity(
                Intent(MainActivity.sContext, MessageActivity::class.java)
                    .putExtra("user_id", it.user_id)
            )
        }

        etSearch.textChanged {
            if (it.isEmpty()) fetchFollow() else fetchSearch(it)
        }
    }

    private fun fetchProfile() {
        viewModel.getUser().observe(this, Observer {
            if (it.imageURL != "default") mImgProfile.loadCircle(it.imageURL)
            mTvName.text = it.name
            mTvStatus.text = it.status
        })
    }

    private fun fetchFollow() {
        mAdapter.typeList = "follow"
        viewModel.getFollows().observe(this, Observer {
            mAdapter.setList(it)
        })
    }

    private fun fetchSearch(name: String) {
        mAdapter.typeList = "search"
        viewModel.getSearch(name).observe(this, Observer {
            mAdapter.setList(it)
        })
    }

    private fun updateFollow(userId: String, type: String) {
        val friend = Follows(userId, type)
        viewModel.setFollow(userId, friend)
    }
}
