package com.comsci.druchat.dialog

import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.comsci.druchat.R
import com.comsci.druchat.util.BaseDialogFragment

class PublicChatsDialog : BaseDialogFragment(
    { R.layout.dialog_public_chats },
    { R.drawable.ic_chat_black },
    { R.string.public_chat }
) {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mIvImage: ImageView
    private lateinit var mEtSend: EditText
    private lateinit var mIvSend: ImageView
    private lateinit var mProgressBar: ProgressBar

    override fun initDialog(view: View) {
        super.initDialog(view)
        mRecyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView
        mIvImage = view.findViewById(R.id.mImgImage) as ImageView
        mEtSend = view.findViewById(R.id.mEdtSend) as EditText
        mIvSend = view.findViewById(R.id.mImgSend) as ImageView
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar
    }

}
