package com.comsci.druchat.ui.login

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.getContent
import com.adedom.library.extension.isEmpty
import com.adedom.library.extension.isLength
import com.adedom.library.extension.toast
import com.adedom.library.util.BaseDialogFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.ui.main.MainActivity

class PhoneDialog : BaseDialogFragment<BaseViewModel>(
    { R.layout.dialog_phone },
    { R.drawable.ic_phone_black },
    { R.string.phone }
) {

    lateinit var mEtCodeSent: EditText
    lateinit var mEtPhoneNumber: EditText
    lateinit var mBtVerificationCode: Button
    lateinit var mBtLogin: Button
    lateinit var mProgressBar: ProgressBar
    var codeSent = ""

    override fun initDialog(view: View) {
        super.initDialog(view)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        mEtCodeSent = view.findViewById(R.id.mEtCodeSent) as EditText
        mEtPhoneNumber = view.findViewById(R.id.mEtPhoneNumber) as EditText
        mBtVerificationCode = view.findViewById(R.id.mBtVerificationCode) as Button
        mBtLogin = view.findViewById(R.id.mBtLogin) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar

        mBtVerificationCode.setOnClickListener {
            sendVerificationCode()
        }
        mBtLogin.setOnClickListener {
            loginByPhone()
        }
    }

    private fun sendVerificationCode() {
        val phone = mEtPhoneNumber.getContent()

        when {
            mEtPhoneNumber.isEmpty(getString(R.string.enter_phone)) -> return
            mEtPhoneNumber.isLength(10, getString(R.string.enter_length_phone_10)) -> return
        }

        mProgressBar.visibility = View.VISIBLE
        dialog!!.setCanceledOnTouchOutside(false)
        mEtPhoneNumber.isEnabled = false
        mBtVerificationCode.isEnabled = false

        viewModel.firebaseVerifyPhoneNumber(activity, "+66${phone.substring(1, 10)}", {
            mProgressBar.visibility = View.INVISIBLE
            mEtCodeSent.setText(it)
            loginByPhone()
        }, {
            mProgressBar.visibility = View.INVISIBLE
            LoginActivity.sContext.toast(it, Toast.LENGTH_LONG)
        }, {
            codeSent = it
            mEtCodeSent.isEnabled = true
            mBtLogin.isEnabled = true
        })
    }

    private fun loginByPhone() {
        val code = mEtCodeSent.getContent()

        when {
            mEtCodeSent.isEmpty(getString(R.string.enter_phone_code)) -> return
            mEtCodeSent.isLength(6, getString(R.string.enter_length_phone_6)) -> return
        }

        mProgressBar.visibility = View.VISIBLE
        mEtCodeSent.isEnabled = false
        mBtLogin.isEnabled = false

        viewModel.firebaseSignInWithCredential(codeSent, code, {
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
