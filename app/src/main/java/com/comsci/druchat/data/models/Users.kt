package com.comsci.druchat.data.models

data class Users(
    val user_id: String = "",
    val name: String = "",
    val status: String = "",
    val imageURL: String = "default",
    val state: String = "offline",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)