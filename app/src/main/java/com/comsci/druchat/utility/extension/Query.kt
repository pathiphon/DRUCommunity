package com.comsci.druchat.utility.extension

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

fun Query.addValueEventListener(onDataChange: (DataSnapshot) -> Unit) {
    this.addValueEventListener(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {}

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            onDataChange.invoke(dataSnapshot)
        }
    })
}

fun Query.addListenerForSingleValueEvent(onDataChange: (DataSnapshot) -> Unit) {
    this.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {}

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            onDataChange.invoke(dataSnapshot)
        }
    })
}