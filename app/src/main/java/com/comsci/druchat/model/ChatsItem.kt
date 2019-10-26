package com.comsci.druchat.model

data class ChatsItem(
    val sender: String = "",
    val receiver: String = "",
    val message: String = "",
    val image: String = "",
    val dateTime: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isread: Boolean = false
)