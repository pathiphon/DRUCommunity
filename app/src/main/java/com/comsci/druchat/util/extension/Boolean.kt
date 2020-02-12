package com.comsci.druchat.util.extension

import java.text.SimpleDateFormat
import java.util.*

fun Boolean.getDateTime(dateTime: Long): String {
    return if (this) {
        SimpleDateFormat("EEE, dd MMM yy", Locale.ENGLISH).format(dateTime)
    } else {
        SimpleDateFormat("HH:mm", Locale.ENGLISH).format(dateTime)
    }
}