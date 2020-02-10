package com.comsci.druchat.dialog

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R
import com.comsci.druchat.util.BaseDialogFragment

class CreateGroupDialog : BaseDialogFragment(
    { R.layout.dialog_create_group },
    { R.drawable.ic_group_black },
    { R.string.create_group }
) {

    private lateinit var mEtName: EditText
    private lateinit var mEtPassword: EditText
    private lateinit var mBtCreateGroup: Button
    private lateinit var mProgressBar: ProgressBar

    override fun initDialog(view: View) {
        super.initDialog(view)
        mEtName = view.findViewById(R.id.mEdtName) as EditText
        mEtPassword = view.findViewById(R.id.mEdtPassword) as EditText
        mBtCreateGroup = view.findViewById(R.id.mBtnCreateGroup) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        mBtCreateGroup.setOnClickListener {
            Toast.makeText(MainActivity.sContext, "Coming soon!!!", Toast.LENGTH_SHORT).show()
        }
    }

}
