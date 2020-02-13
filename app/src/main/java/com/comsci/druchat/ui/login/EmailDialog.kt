package com.comsci.druchat.ui.login

import android.content.Intent
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.getContent
import com.adedom.library.extension.toast
import com.adedom.library.util.BaseDialogFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.ui.main.MainActivity

class EmailDialog : BaseDialogFragment<BaseViewModel>(
    { R.layout.dialog_email },
    { R.drawable.ic_email_black },
    { R.string.e_mail }
) {

    private lateinit var mEtEmail: EditText
    private lateinit var mEtPassword: EditText
    private lateinit var mBtReg: Button
    private lateinit var mBtLogin: Button
    private lateinit var mTvForgotPassword: TextView
    private lateinit var mProgressBar: ProgressBar

    override fun initDialog(view: View) {
        super.initDialog(view)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        mEtEmail = view.findViewById(R.id.mEtEmail) as EditText
        mEtPassword = view.findViewById(R.id.mEtPassword) as EditText
        mBtReg = view.findViewById(R.id.mBtReg) as Button
        mBtLogin = view.findViewById(R.id.mBtLogin) as Button
        mTvForgotPassword = view.findViewById(R.id.mTvForgotPassword) as TextView
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        mTvForgotPassword.setOnClickListener {
            ForgotPasswordDialog().show(activity!!.supportFragmentManager, null)
        }
        mBtReg.setOnClickListener {
            RegisterUserDialog().show(activity!!.supportFragmentManager, null)
        }
        mBtLogin.setOnClickListener { login() }
    }

    private fun login() {
        val email = mEtEmail.getContent()
        val password = mEtPassword.getContent()

        mProgressBar.visibility = View.VISIBLE

        if (email.isEmpty() || password.isEmpty()) {
            LoginActivity.sContext.toast(R.string.filed_required, Toast.LENGTH_LONG)
        } else {
            viewModel.firebaseSignInWithEmailAndPassword(email, password, {
                mProgressBar.visibility = View.INVISIBLE
                activity!!.finish()
                startActivity(
                    Intent(LoginActivity.sContext, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }, {
                mProgressBar.visibility = View.INVISIBLE
                LoginActivity.sContext.toast(it, Toast.LENGTH_LONG)
            })
        }
    }

}
