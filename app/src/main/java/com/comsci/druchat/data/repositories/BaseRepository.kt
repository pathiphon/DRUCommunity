package com.comsci.druchat.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.comsci.druchat.data.models.Follows
import com.comsci.druchat.data.models.Users
import com.comsci.druchat.utility.extension.addListenerForSingleValueEvent
import com.comsci.druchat.utility.extension.addValueEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class BaseRepository {

    val currentUser = FirebaseAuth.getInstance().currentUser
    private val mDatabase = FirebaseDatabase.getInstance()
    private val mUsers = mDatabase.getReference("Users")
    private val mFollow = mDatabase.getReference("Follow")

    fun getUser(): LiveData<Users> {
        val liveData = MutableLiveData<Users>()
        mUsers.child(currentUser!!.uid).addValueEventListener {
            liveData.value = it.getValue(Users::class.java)
        }
        return liveData
    }

    fun getUsers(): LiveData<List<Users>> {
        val liveData = MutableLiveData<List<Users>>()
        mUsers.addValueEventListener {
            val items = arrayListOf<Users>()
            for (snapshot in it.children) {
                val item = snapshot.getValue(Users::class.java)
                items.add(item!!)
            }
            liveData.value = items
        }
        return liveData
    }

    fun getSearch(name: String): LiveData<List<Users>> {
        val liveData = MutableLiveData<List<Users>>()
        mUsers.orderByChild("name").startAt(name).endAt(name + "\uf8ff").addListenerForSingleValueEvent {
            val items = arrayListOf<Users>()
            for (snapshot in it.children) {
                val item = snapshot.getValue(Users::class.java)
                if (currentUser!!.uid != item!!.user_id) items.add(item)
            }
            liveData.value = items
        }
        return liveData
    }

    fun getFollows(): LiveData<List<Users>> {
        val liveData = MutableLiveData<List<Users>>()
        mFollow.child(currentUser!!.uid).addValueEventListener { dataSnapshot ->
            val items = arrayListOf<Users>()

            for (snapshot in dataSnapshot.children) {
                val follow = snapshot.getValue(Follows::class.java)

                if (follow!!.type == "follow") {
                    mUsers.child(follow.user_id).addListenerForSingleValueEvent {
                        val item = it.getValue(Users::class.java)
                        items.add(item!!)
                    }
                }
            }

            liveData.value = items
        }
        return liveData
    }

    fun setLatlng(hashMap: HashMap<String, Any>) {
        mUsers.child(currentUser!!.uid).updateChildren(hashMap)
    }

    fun setState(state: String) {
        mUsers.child(currentUser!!.uid).child("state").setValue(state)
    }

    fun setFollow(followId: String, friend: Follows) {
        mFollow.child(currentUser!!.uid).child(followId).setValue(friend)
    }

}



