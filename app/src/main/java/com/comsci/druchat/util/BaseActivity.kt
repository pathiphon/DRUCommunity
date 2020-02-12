package com.comsci.druchat.util

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.toast
import com.adedom.library.util.PathiphonActivity
import com.comsci.druchat.data.viewmodel.BaseViewModel

abstract class BaseActivity : PathiphonActivity() {

    lateinit var viewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)
    }

    override fun onResume() {
        viewModel.setState(KEY_ONLINE) { baseContext.toast(it, Toast.LENGTH_LONG) }
        super.onResume()
    }

    override fun onPause() {
        viewModel.setState(KEY_OFFLINE) { baseContext.toast(it, Toast.LENGTH_LONG) }
        super.onPause()
    }

}

