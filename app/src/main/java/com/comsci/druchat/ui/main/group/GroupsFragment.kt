package com.comsci.druchat.ui.main.group

import android.view.View
import androidx.lifecycle.ViewModelProviders
import com.adedom.library.util.BaseFragment
import com.comsci.druchat.R
import com.comsci.druchat.data.viewmodel.BaseViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GroupsFragment : BaseFragment<BaseViewModel>({ R.layout.fragment_groups }) {

    override fun initFragment(view: View) {
        super.initFragment(view)
        viewModel = ViewModelProviders.of(this).get(BaseViewModel::class.java)

        view.findViewById<FloatingActionButton>(R.id.mFloatingActionButton).setOnClickListener {
            CreateGroupDialog()
                .show(activity!!.supportFragmentManager, null)
        }
    }

}
