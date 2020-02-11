package com.comsci.druchat.fragments

import android.view.View
import com.comsci.druchat.R
import com.comsci.druchat.dialog.CreateGroupDialog
import com.comsci.druchat.util.BaseFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GroupsFragment : BaseFragment({ R.layout.fragment_groups }) {

    override fun initFragment(view: View) {
        super.initFragment(view)
        view.findViewById<FloatingActionButton>(R.id.mFloatingActionButton).setOnClickListener {
            CreateGroupDialog().show(activity!!.supportFragmentManager, null)
        }
    }

}
