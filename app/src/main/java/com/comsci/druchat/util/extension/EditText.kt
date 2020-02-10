package com.comsci.druchat.util.extension

import android.util.Patterns
import android.widget.EditText
import com.adedom.library.extension.getContent

fun EditText.isEmail(error: String): Boolean {
    if (!Patterns.EMAIL_ADDRESS.matcher(this.getContent()).matches()) {
        this.requestFocus()
        this.error = error
        return true
    }
    return false
}
