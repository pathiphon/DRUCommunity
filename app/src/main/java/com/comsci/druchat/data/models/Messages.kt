package com.comsci.druchat.data.models

data class Messages(
    val sender: String = "",
    val receiver: String = "",
    val message: String = "",
    val image: String = "",
    val dateTime: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isread: Boolean = false
)