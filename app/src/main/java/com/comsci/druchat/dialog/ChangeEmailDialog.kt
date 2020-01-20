package com.comsci.druchat.dialog

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.comsci.druchat.LoginActivity
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangeEmailDialog : DialogFragment() {

    private lateinit var mUser: FirebaseUser
    private lateinit var mEdtNewEmail: EditText
    private lateinit var mEdtConfirmPassword: EditText
    private lateinit var mBtnChangeEmail: Button
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_change_email, null)

        val builder = AlertDialog.Builder(activity!!)
            .setView(view)
            .setTitle(R.string.e_mail)
            .setIcon(R.drawable.ic_settings_black)

        mUser = FirebaseAuth.getInstance().currentUser!!

        bindWidgets(view)
        setEvents()

        return builder.create()
    }

    private fun bindWidgets(view: View) {
        mEdtNewEmail = view.findViewById(R.id.mEdtNewEmail) as EditText
        mEdtConfirmPassword = view.findViewById(R.id.mEdtConfirmPassword) as EditText
        mBtnChangeEmail = view.findViewById(R.id.mBtnChangeEmail) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar
    }

    private fun setEvents() = mBtnChangeEmail.setOnClickListener { changeEmail() }

    private fun changeEmail() {
        val email = mEdtNewEmail.text.toString().trim()
        val confirmPassword = mEdtConfirmPassword.text.toString().trim()

        when {
            email.isEmpty() -> {
                mEdtNewEmail.error = "Please enter New e-mail"
                mEdtNewEmail.requestFocus()
                return
            }
            confirmPassword.isEmpty() -> {
                mEdtConfirmPassword.error = "Please enter Confirm password"
                mEdtConfirmPassword.requestFocus()
                return
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                mEdtNewEmail.error = "Please enter a valid email"
                mEdtNewEmail.requestFocus()
                return
            }
        }

        mProgressBar.visibility = View.VISIBLE
        dialog!!.setCanceledOnTouchOutside(false)
        mEdtNewEmail.isEnabled = false
        mEdtConfirmPassword.isEnabled = false
        mBtnChangeEmail.isEnabled = false

        val credential = EmailAuthProvider.getCredential(mUser.email!!, confirmPassword)
        mUser.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mUser.updateEmail(email).addOnCompleteListener { task ->
                    dialog!!.dismiss()
                    mProgressBar.visibility = View.GONE
                    if (task.isSuccessful) {
                        FirebaseAuth.getInstance().signOut()
                        activity!!.finish()
                        startActivity(
                            Intent(MainActivity.mContext, LoginActivity::class.java)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        )
                    } else {
                        Toast.makeText(MainActivity.mContext, task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                dialog!!.dismiss()
                mProgressBar.visibility = View.GONE
                Toast.makeText(MainActivity.mContext, task.exception!!.message, Toast.LENGTH_LONG).show()
            }
        }
    }

}
