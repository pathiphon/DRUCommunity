package com.comsci.druchat.ui.login

import android.view.View
import com.adedom.library.util.BaseDialogFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel

class PhoneDialog : BaseDialogFragment<BaseViewModel>(
    {R.layout.dialog_phone},
    {R.drawable.ic_phone_black},
    {R.string.phone}
) {

    override fun initDialog(view: View) {
        super.initDialog(view)

    }

}
