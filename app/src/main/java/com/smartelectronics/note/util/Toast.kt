package com.smartelectronics.note.util

import android.content.Context
import android.widget.Toast

fun Context.showMessage(message: CharSequence){

    Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
}