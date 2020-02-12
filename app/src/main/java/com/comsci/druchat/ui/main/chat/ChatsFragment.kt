package com.comsci.druchat.ui.main.chat

import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.adedom.library.extension.recyclerVertical
import com.adedom.library.util.BaseFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.ui.main.MainActivity
import com.comsci.druchat.ui.messages.MessageActivity
import com.comsci.druchat.util.KEY_USER_ID

class ChatsFragment : BaseFragment<BaseViewModel>({ R.layout.fragment_chats }) {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: ChatAdapter

    override fun initFragment(view: View) {
        super.initFragment(view)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        mRecyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView

        mAdapter = ChatAdapter()
        mRecyclerView.recyclerVertical { it.adapter = mAdapter }

        mAdapter.onClick = {
            startActivity(
                Intent(MainActivity.sContext, MessageActivity::class.java)
                    .putExtra(KEY_USER_ID, it)
            )
        }

        fetchChatList()
    }

    private fun fetchChatList() {
        //todo count no read
        viewModel.getChatListUsers().observe(this, Observer {
            mAdapter.setList(it)
        })
    }

}