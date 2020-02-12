package com.comsci.druchat

import org.junit.Test
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MessagesUnitTest {

    @Test
    fun dateTimeTest() {
        val dd = SimpleDateFormat("EEE, dd MMM yy HH:mm", Locale.ENGLISH)
            .format(System.currentTimeMillis())
        println("dd>> $dd")

    }

}
