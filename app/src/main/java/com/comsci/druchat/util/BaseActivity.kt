package com.comsci.druchat.util

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.comsci.druchat.data.viewmodel.BaseViewModel

abstract class BaseActivity : AppCompatActivity() {

    lateinit var viewModel: BaseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)
    }

    override fun onResume() {
        viewModel.setState("online")
        super.onResume()
    }

    override fun onPause() {
        viewModel.setState("offline")
        super.onPause()
    }

}

