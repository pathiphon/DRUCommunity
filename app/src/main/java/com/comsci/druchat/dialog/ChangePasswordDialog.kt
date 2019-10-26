package com.comsci.druchat.dialog

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.comsci.druchat.LoginActivity
import com.comsci.druchat.MainActivity
import com.comsci.druchat.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangePasswordDialog : DialogFragment() {

    private lateinit var mUser: FirebaseUser
    private lateinit var mEdtOldPassword: EditText
    private lateinit var mEdtNewPassword: EditText
    private lateinit var mEdtRePassword: EditText
    private lateinit var mBtnChangePassword: Button
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_change_password, null)

        val builder = AlertDialog.Builder(activity!!)
            .setView(view)
            .setTitle(R.string.change_password)
            .setIcon(R.drawable.ic_settings_black)

        mUser = FirebaseAuth.getInstance().currentUser!!

        bindWidgets(view)
        setEvents()

        return builder.create()
    }

    private fun bindWidgets(view: View) {
        mEdtOldPassword = view.findViewById(R.id.mEdtOldPassword) as EditText
        mEdtNewPassword = view.findViewById(R.id.mEdtNewPassword) as EditText
        mEdtRePassword = view.findViewById(R.id.mEdtRePassword) as EditText
        mBtnChangePassword = view.findViewById(R.id.mBtnChangePassword) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar
    }

    private fun setEvents() = mBtnChangePassword.setOnClickListener { changePassword() }

    private fun changePassword() {
        val oldPassword = mEdtOldPassword.text.toString().trim()
        val newPassword = mEdtNewPassword.text.toString().trim()
        val rePassword = mEdtRePassword.text.toString().trim()

        when {
            oldPassword.isEmpty() -> {
                mEdtOldPassword.error = "Please enter Old password"
                mEdtOldPassword.requestFocus()
                return
            }
            newPassword.isEmpty() -> {
                mEdtNewPassword.error = "Please enter Password"
                mEdtNewPassword.requestFocus()
                return
            }
            rePassword.isEmpty() -> {
                mEdtRePassword.error = "Please enter Re-password"
                mEdtRePassword.requestFocus()
                return
            }
            newPassword.length < 8 -> {
                mEdtNewPassword.error = "Minimum length of password should be 8"
                mEdtNewPassword.requestFocus()
                return
            }
            newPassword != rePassword -> {
                mEdtRePassword.error = "Passwords do not match"
                mEdtRePassword.requestFocus()
                return
            }
        }

        mProgressBar.visibility = View.VISIBLE
        dialog.setCanceledOnTouchOutside(false)
        mEdtOldPassword.isEnabled = false
        mEdtNewPassword.isEnabled = false
        mEdtRePassword.isEnabled = false
        mBtnChangePassword.isEnabled = false

        val credential = EmailAuthProvider.getCredential(mUser.email!!, oldPassword)
        mUser.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mUser.updatePassword(newPassword).addOnCompleteListener { task ->
                    dialog.dismiss()
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
                dialog.dismiss()
                mProgressBar.visibility = View.GONE
                Toast.makeText(MainActivity.mContext, task.exception!!.message, Toast.LENGTH_LONG).show()
            }
        }
    }

}
