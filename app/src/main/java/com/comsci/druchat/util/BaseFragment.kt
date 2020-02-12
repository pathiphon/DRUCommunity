package com.comsci.druchat.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.comsci.druchat.data.viewmodel.BaseViewModel

abstract class BaseFragment(private val recource: () -> Int) : Fragment() {

    lateinit var viewModel: BaseViewModel
    lateinit var v: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        v = inflater.inflate(recource.invoke(), container, false)

        initFragment(v)

        return v
    }

    open fun initFragment(view: View) {}

}