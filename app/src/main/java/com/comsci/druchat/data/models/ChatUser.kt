package com.comsci.druchat.data.models

data class ChatUser(
    val user_id: String = "",
    val name: String = "",
    val imageURL: String = "default",
    var count: Int = 0
)