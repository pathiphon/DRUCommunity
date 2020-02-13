package com.comsci.druchat.ui.main.group

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.failed
import com.adedom.library.util.BaseDialogFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.ui.main.MainActivity

class CreateGroupDialog : BaseDialogFragment<BaseViewModel>(
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
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        mEtName = view.findViewById(R.id.mEdtName) as EditText
        mEtPassword = view.findViewById(R.id.mEtPassword) as EditText
        mBtCreateGroup = view.findViewById(R.id.mBtnCreateGroup) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        mBtCreateGroup.setOnClickListener {
            MainActivity.sContext.failed()
        }
    }

}
