package com.comsci.druchat.ui.main.profile

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.*
import com.adedom.library.util.BaseDialogFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.ui.login.LoginActivity
import com.comsci.druchat.ui.main.MainActivity

class ChangePasswordDialog : BaseDialogFragment<BaseViewModel>(
    { R.layout.dialog_change_password },
    { R.drawable.ic_settings_black },
    { R.string.change_password }
) {

    private lateinit var mEtOldPassword: EditText
    private lateinit var mEtNewPassword: EditText
    private lateinit var mEtRePassword: EditText
    private lateinit var mBtChangePassword: Button
    private lateinit var mProgressBar: ProgressBar

    override fun initDialog(view: View) {
        super.initDialog(view)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        mEtOldPassword = view.findViewById(R.id.mEdtOldPassword) as EditText
        mEtNewPassword = view.findViewById(R.id.mEdtNewPassword) as EditText
        mEtRePassword = view.findViewById(R.id.mEdtRePassword) as EditText
        mBtChangePassword = view.findViewById(R.id.mBtnChangePassword) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        mBtChangePassword.setOnClickListener { changePassword() }
    }

    private fun changePassword() {
        val oldPassword = mEtOldPassword.getContent()
        val newPassword = mEtNewPassword.getContent()

        when {
            mEtOldPassword.isEmpty(getString(R.string.enter_old_password)) -> return
            mEtNewPassword.isEmpty(getString(R.string.enter_new_password)) -> return
            mEtRePassword.isEmpty(getString(R.string.enter_re_password)) -> return
            mEtNewPassword.isLength(8, getString(R.string.enter_length_password_8)) -> return
            mEtNewPassword.isMatching(mEtOldPassword, getString(R.string.enter_not_match)) -> return
        }

        mProgressBar.visibility = View.VISIBLE
        dialog!!.setCanceledOnTouchOutside(false)
        mEtOldPassword.isEnabled = false
        mEtNewPassword.isEnabled = false
        mEtRePassword.isEnabled = false
        mBtChangePassword.isEnabled = false

        viewModel.firebaseUpdatePassword(oldPassword, newPassword, {
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
