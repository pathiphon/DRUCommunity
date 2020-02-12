package com.comsci.druchat.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.toast
import com.adedom.library.util.PathiphonActivity
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.ui.main.MainActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : PathiphonActivity() {

    private lateinit var viewModel: BaseViewModel

    companion object {
        lateinit var mContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        mContext = baseContext

        mLogin.startAnimation(
            AnimationUtils.loadAnimation(
                mContext,
                R.anim.fade_in
            )
        )

        init()

    }

    private fun init() {
        mTvForgotPassword.setOnClickListener {
            ForgotPasswordDialog().show(
                supportFragmentManager,
                null
            )
        }
        mBtnReg.setOnClickListener {
            RegisterUserDialog()
                .show(supportFragmentManager, null)
        }
        mBtnLogin.setOnClickListener { login() }
    }

    private fun login() {
        val email = mEdtEmail.text.toString().trim()
        val password = mEdtPassword.text.toString().trim()

        mProgressBar.visibility = View.VISIBLE

        if (email.isEmpty() || password.isEmpty()) {
            baseContext.toast(R.string.filed_required, Toast.LENGTH_LONG)
        } else {
            viewModel.firebaseSignInWithEmailAndPassword(email, password, {
                mProgressBar.visibility = View.INVISIBLE
                finish()
                startActivity(
                    Intent(baseContext, MainActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }, {
                mProgressBar.visibility = View.INVISIBLE
                baseContext.toast(it, Toast.LENGTH_LONG)
            })
        }
    }
}
