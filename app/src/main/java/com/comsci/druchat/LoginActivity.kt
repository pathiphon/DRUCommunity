package com.comsci.druchat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.toast
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.comsci.druchat.dialog.ForgotPasswordDialog
import com.comsci.druchat.dialog.RegisterUserDialog
import com.comsci.druchat.util.MyCode
import com.luseen.simplepermission.permissions.Permission
import com.luseen.simplepermission.permissions.PermissionActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : PermissionActivity() {

    private lateinit var viewModel: BaseViewModel

    companion object {
        lateinit var mContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

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
        mTvForgotPassword.setOnClickListener {
            ForgotPasswordDialog().show(
                supportFragmentManager,
                null
            )
        }
        mBtnReg.setOnClickListener { RegisterUserDialog().show(supportFragmentManager, null) }
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
