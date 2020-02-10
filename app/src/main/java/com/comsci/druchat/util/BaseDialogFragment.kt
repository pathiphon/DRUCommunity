package com.comsci.druchat.util

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.extension.dialogFragment
import com.comsci.druchat.data.viewmodel.BaseViewModel

abstract class BaseDialogFragment(
    private val resource: () -> Int,
    private val icon: () -> Int,
    private val title: () -> Int
) : DialogFragment() {

    lateinit var v: View
    lateinit var viewModel: BaseViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)
        v = activity!!.layoutInflater.inflate(resource.invoke(), null)
        initDialog(v)
        return AlertDialog.Builder(activity!!).dialogFragment(v, icon.invoke(), title.invoke())
    }

    open fun initDialog(view: View) {}

}
