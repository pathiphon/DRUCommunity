package com.comsci.druchat.ui.login

import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.getContent
import com.adedom.library.extension.isEmail
import com.adedom.library.extension.isEmpty
import com.adedom.library.extension.toast
import com.adedom.library.util.BaseDialogFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.ui.main.MainActivity

class ForgotPasswordDialog : BaseDialogFragment<BaseViewModel>(
    { R.layout.dialog_forgot_password },
    { R.drawable.ic_settings_black },
    { R.string.reset }
) {

    private lateinit var mEtEmail: EditText
    private lateinit var mBtReset: Button
    private lateinit var mProgressBar: ProgressBar

    override fun initDialog(view: View) {
        super.initDialog(view)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        mEtEmail = view.findViewById(R.id.mEdtEmail) as EditText
        mBtReset = view.findViewById(R.id.mBtnReset) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        mBtReset.setOnClickListener { changePasswordFromEmail() }
    }

    private fun changePasswordFromEmail() {
        val email = mEtEmail.getContent()

        when {
            mEtEmail.isEmpty(getString(R.string.enter_email)) -> return
            mEtEmail.isEmail(getString(R.string.enter_valid_email)) -> return
        }

        mProgressBar.visibility = View.VISIBLE
        dialog!!.setCanceledOnTouchOutside(false)
        mEtEmail.isEnabled = false
        mBtReset.isEnabled = false

        viewModel.firebaseSendPasswordResetEmail(email, {
            mProgressBar.visibility = View.GONE
            dialog!!.dismiss()
            MainActivity.sContext.toast(getString(R.string.check_your_email))
        }, {
            mProgressBar.visibility = View.GONE
            dialog!!.dismiss()
            MainActivity.sContext.toast(it)
        })
    }
}
