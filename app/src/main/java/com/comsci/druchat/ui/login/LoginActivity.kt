package com.comsci.druchat.ui.login

import android.content.Context
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.util.PathiphonActivity
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : PathiphonActivity() {

    private lateinit var viewModel: BaseViewModel

    companion object {
        lateinit var sContext: Context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        sContext = baseContext

        init()

    }

    private fun init() {
        mLogin.startAnimation(
            AnimationUtils.loadAnimation(
                sContext,
                R.anim.fade_in
            )
        )

        mBtEmail.setOnClickListener {
            EmailDialog().show(supportFragmentManager, null)
        }

        mBtPhone.setOnClickListener {
            PhoneDialog().show(supportFragmentManager, null)
        }
    }

}
