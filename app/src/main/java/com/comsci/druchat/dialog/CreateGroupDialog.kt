package com.comsci.druchat.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R

class CreateGroupDialog : DialogFragment() {

    private lateinit var mEdtName: EditText
    private lateinit var mEdtPassword: EditText
    private lateinit var mBtnCreateGroup: Button
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_create_group, null)

        val builder = AlertDialog.Builder(activity!!)
            .setView(view)
            .setTitle(R.string.create_group)
            .setIcon(R.drawable.ic_group_black)

        bindWidgets(view)
        setEvents()

        return builder.create()
    }

    private fun bindWidgets(view: View) {
        mEdtName = view.findViewById(R.id.mEdtName) as EditText
        mEdtPassword = view.findViewById(R.id.mEdtPassword) as EditText
        mBtnCreateGroup = view.findViewById(R.id.mBtnCreateGroup) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar
    }

    private fun setEvents() {
        mBtnCreateGroup.setOnClickListener {
            Toast.makeText(MainActivity.mContext, "Coming soon!!!", Toast.LENGTH_SHORT).show()
        }
    }

}
