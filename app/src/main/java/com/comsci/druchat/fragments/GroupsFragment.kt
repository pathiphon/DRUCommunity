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

    private lateinit var mFloatingActionButton: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groups, container, false)

        bindWidgets(view)
        setEvents()

        return view
    }

    private fun bindWidgets(view: View) {
        mFloatingActionButton = view.findViewById(R.id.mFloatingActionButton) as FloatingActionButton
    }

    private fun setEvents() = mFloatingActionButton.setOnClickListener {
        CreateGroupDialog().show(activity!!.supportFragmentManager, null)
    }
}
