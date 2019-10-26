package com.comsci.druchat.dialog

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import com.comsci.druchat.R

class PublicChatsDialog : DialogFragment() {

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mImgImage: ImageView
    private lateinit var mEdtSend: EditText
    private lateinit var mImgSend: ImageView
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)

        val view = activity!!.layoutInflater.inflate(R.layout.dialog_public_chats, null)

        val builder = AlertDialog.Builder(activity!!)
            .setView(view)
            .setIcon(R.drawable.ic_chat_black)
            .setTitle(R.string.public_chat)

        bindWidgets(view)
        setEvents()

        return builder.create()
    }

    private fun bindWidgets(view: View) {
        mRecyclerView = view.findViewById(R.id.mRecyclerView) as RecyclerView
        mImgImage = view.findViewById(R.id.mImgImage) as ImageView
        mEdtSend = view.findViewById(R.id.mEdtSend) as EditText
        mImgSend = view.findViewById(R.id.mImgSend) as ImageView
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar
    }

    private fun setEvents() {

    }

}
