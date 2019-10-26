package com.comsci.druchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.comsci.druchat.dialog.ForgotPasswordDialog
import com.comsci.druchat.dialog.InsertUserDialog
import com.comsci.druchat.utility.MyCode
import com.google.firebase.auth.FirebaseAuth
import com.luseen.simplepermission.permissions.Permission
import com.luseen.simplepermission.permissions.PermissionActivity
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : PermissionActivity() {

    companion object {
        lateinit var mContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mContext = baseContext

        mLogin.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in))

        setApp()
        setEvents()
    }

    //region setApp
    private fun setApp() {
        requestPermission(Permission.FINE_LOCATION) { permissionGranted, isPermissionDeniedForever ->
            if (!permissionGranted) {
                Toast.makeText(mContext, "Please grant permission", Toast.LENGTH_LONG).show()
                finishAffinity()
            }
        }

        if (!MyCode.locationSetting(mContext)) {
            startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1234)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (!MyCode.locationSetting(mContext) && requestCode == 1234) {
            Toast.makeText(mContext, "Please grant location", Toast.LENGTH_LONG).show()
            finishAffinity()
        }
    }
    //endregion

    private fun setEvents() {
        mTvForgotPassword.setOnClickListener { ForgotPasswordDialog().show(supportFragmentManager, null) }
        mBtnReg.setOnClickListener { InsertUserDialog().show(supportFragmentManager, null) }
        mBtnLogin.setOnClickListener { login() }
    }

    private fun login() {
        val email = mEdtEmail.text.toString().trim()
        val password = mEdtPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(baseContext, "All filed are required", Toast.LENGTH_LONG).show()
        } else {
            mProgressBar.visibility = View.VISIBLE
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                mProgressBar.visibility = View.INVISIBLE
                if (task.isSuccessful) {
                    startActivity(
                        Intent(baseContext, MainActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    )
                    finish()
                } else {
                    Toast.makeText(baseContext, task.exception!!.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
