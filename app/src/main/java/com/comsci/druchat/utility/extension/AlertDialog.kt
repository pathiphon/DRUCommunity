package com.comsci.druchat.utility.extension

import androidx.appcompat.app.AlertDialog
import com.comsci.druchat.R

fun AlertDialog.Builder.dialogPositive(
    title: Int,
    message: Int,
    icon: Int,
    positive: () -> Unit
) {
    this.setTitle(title)
        .setMessage(message)
        .setIcon(icon)
        .setPositiveButton(R.string.yes) { dialog, which -> positive.invoke() }
        .setNegativeButton(R.string.no) { dialog, which -> dialog.dismiss() }
        .show()
}

fun AlertDialog.Builder.dialogPositive(
    title: Int,
    message: String,
    icon: Int,
    positive: () -> Unit
) {
    this.setTitle(title)
        .setMessage(message)
        .setIcon(icon)
        .setPositiveButton(R.string.yes) { dialog, which -> positive.invoke() }
        .setNegativeButton(R.string.no) { dialog, which -> dialog.dismiss() }
        .show()
}

fun AlertDialog.Builder.dialogPositive(
    title: Int,
    positive: () -> Unit
) {
    this.setTitle(title)
        .setPositiveButton(R.string.yes) { dialog, which -> positive.invoke() }
        .setNegativeButton(R.string.no) { dialog, which -> dialog.dismiss() }
        .show()
}
