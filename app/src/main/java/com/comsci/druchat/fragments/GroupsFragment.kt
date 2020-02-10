package com.comsci.druchat.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.comsci.druchat.R
import com.comsci.druchat.dialog.CreateGroupDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class GroupsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)

        init(view)

        return view
    }

    private fun init(view: View) {
        view.findViewById<FloatingActionButton>(R.id.mFloatingActionButton).setOnClickListener {
            CreateGroupDialog().show(activity!!.supportFragmentManager, null)
        }
    }
}
