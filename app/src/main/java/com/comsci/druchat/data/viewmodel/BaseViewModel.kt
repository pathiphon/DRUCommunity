package com.comsci.druchat.data.viewmodel

import androidx.lifecycle.ViewModel
import com.comsci.druchat.data.models.Follows
import com.comsci.druchat.data.repositories.BaseRepository

class BaseViewModel : ViewModel() {

    private val repo = BaseRepository()

    fun currentUser() = repo.currentUser

    fun getUser() = repo.getUser()

    fun getUsers() = repo.getUsers()

    fun getSearch(name: String) = repo.getSearch(name)

    fun getFollows() = repo.getFollows()

    fun setLatlng(hashMap: HashMap<String, Any>) = repo.setLatlng(hashMap)

    fun setState(state: String) = repo.setState(state)

    fun setFollow(followId: String, friend: Follows) = repo.setFollow(followId, friend)

}

