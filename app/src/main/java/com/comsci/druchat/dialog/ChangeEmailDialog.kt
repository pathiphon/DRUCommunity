package com.comsci.druchat.dialog

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import com.adedom.library.extension.getContent
import com.adedom.library.extension.isEmpty
import com.adedom.library.extension.toast
import com.comsci.druchat.LoginActivity
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R
import com.comsci.druchat.util.BaseDialogFragment
import com.comsci.druchat.util.extension.isEmail

class ChangeEmailDialog : BaseDialogFragment(
    { R.layout.dialog_change_email },
    { R.drawable.ic_settings_black },
    { R.string.e_mail }
) {

    private lateinit var mEtNewEmail: EditText
    private lateinit var mEtConfirmPassword: EditText
    private lateinit var mBtChangeEmail: Button
    private lateinit var mProgressBar: ProgressBar

    override fun initDialog(view: View) {
        super.initDialog(view)
        mEtNewEmail = view.findViewById(R.id.mEdtNewEmail) as EditText
        mEtConfirmPassword = view.findViewById(R.id.mEdtConfirmPassword) as EditText
        mBtChangeEmail = view.findViewById(R.id.mBtnChangeEmail) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        mBtChangeEmail.setOnClickListener { changeEmail() }
    }

    private fun changeEmail() {
        val email = mEtNewEmail.getContent()
        val confirmPassword = mEtConfirmPassword.getContent()

        when {
            mEtNewEmail.isEmpty(getString(R.string.enter_new_email)) -> return
            mEtConfirmPassword.isEmpty(getString(R.string.enter_confirm_password)) -> return
            mEtNewEmail.isEmail(getString(R.string.enter_valid_email)) -> return
        }

        mProgressBar.visibility = View.VISIBLE
        dialog!!.setCanceledOnTouchOutside(false)
        mEtNewEmail.isEnabled = false
        mEtConfirmPassword.isEnabled = false
        mBtChangeEmail.isEnabled = false

        viewModel.firebaseUpdateEmail(confirmPassword, email, {
            mProgressBar.visibility = View.GONE
            dialog!!.dismiss()
            viewModel.firebaseAuth().signOut()
            activity!!.finish()
            startActivity(
                Intent(MainActivity.sContext, LoginActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }, {
            mProgressBar.visibility = View.GONE
            dialog!!.dismiss()
            MainActivity.sContext.toast(it)
        })
    }

}
