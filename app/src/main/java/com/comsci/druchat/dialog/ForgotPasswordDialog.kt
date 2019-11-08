package com.comsci.druchat.dialog

import android.app.Dialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.comsci.druchat.R
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordDialog : DialogFragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mEdtEmail: EditText
    private lateinit var mBtnReset: Button
    private lateinit var mProgressBar: ProgressBar

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity!!.layoutInflater.inflate(R.layout.dialog_forgot_password, null)

        val builder = AlertDialog.Builder(activity!!)
            .setView(view)
            .setTitle(R.string.reset)
            .setIcon(R.drawable.ic_settings_black)

        mAuth = FirebaseAuth.getInstance()

        bindWidgets(view)
        setEvents()

        return builder.create()
    }

    private fun bindWidgets(view: View) {
        mEdtEmail = view.findViewById(R.id.mEdtEmail) as EditText
        mBtnReset = view.findViewById(R.id.mBtnReset) as Button
        mProgressBar = view.findViewById(R.id.mProgressBar) as ProgressBar
    }

    private fun setEvents() {
        mBtnReset.setOnClickListener {
            mProgressBar.visibility = View.VISIBLE
            dialog.setCanceledOnTouchOutside(false)
            mEdtEmail.isEnabled = false
            mBtnReset.isEnabled = false

            val email = mEdtEmail.text.toString()
            when {
                email.isEmpty() -> {
                    mEdtEmail.error = "Please enter E-mail"
                    mEdtEmail.requestFocus()
                    return@setOnClickListener
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    mEdtEmail.error = "Please enter a valid email"
                    mEdtEmail.requestFocus()
                    return@setOnClickListener
                }
                else -> {
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                        mProgressBar.visibility = View.INVISIBLE
                        if (task.isSuccessful) {
                            dialog.dismiss()
                            Toast.makeText(context, "Please check you Email", Toast.LENGTH_LONG).show()
                        } else {
                            dialog.setCancelable(true)
                            mEdtEmail.isEnabled = true
                            mBtnReset.isEnabled = true

                            Toast.makeText(context, task.exception!!.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}
