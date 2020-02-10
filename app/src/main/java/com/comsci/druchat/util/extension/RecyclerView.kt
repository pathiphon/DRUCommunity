package com.comsci.druchat.util.extension

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.recyclerViewChats(rv: (RecyclerView) -> Unit) {
    this.apply {
        val lm = LinearLayoutManager(context)
        lm.stackFromEnd = true
        layoutManager = lm
        rv.invoke(this)
    }
}
